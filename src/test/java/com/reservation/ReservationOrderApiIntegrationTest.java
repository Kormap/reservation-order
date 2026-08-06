package com.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.member.domain.Member;
import com.reservation.member.domain.MemberRepository;
import com.reservation.member.domain.MemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ReservationOrderApiIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void OpenAPI_요청_스키마가_DTO별로_분리된다() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/v1/products']['post']['requestBody']['content']['application/json']['schema']['$ref']")
                        .value("#/components/schemas/ProductCreateRequest"))
                .andExpect(jsonPath("$['components']['schemas']['ProductCreateRequest']['properties']['categoryCode']['enum'][0]")
                        .value("APPLIANCE"))
                .andExpect(jsonPath("$['components']['schemas']['ProductCreateRequest']['properties']['categoryCode']['enum'][8]")
                        .value("DIGITAL"))
                .andExpect(jsonPath("$['paths']['/api/v1/orders']['post']['requestBody']['content']['application/json']['schema']['$ref']")
                        .value("#/components/schemas/ReservationOrderCreateRequest"));
    }

    @Test
    void Security_예외를_표준_오류_응답으로_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        MockHttpSession adminSession = createAdminAndLogin("security-admin@example.com", "관리자");
        mockMvc.perform(post("/api/v1/orders")
                        .session(adminSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productId":1,"quantity":1}]}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void 잘못된_로그인_정보는_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"wrong-password@example.com","password":"password123","name":"회원"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"wrong-password@example.com","password":"incorrect-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void 잘못된_로그인_요청은_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"invalid-email","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void 회원가입부터_예약과_취소까지_처리한다() throws Exception {
        MockHttpSession adminSession = createAdminAndLogin("mvp-admin@example.com", "관리자");
        MockHttpSession memberSession = signUpAndLogin("member@example.com", "회원");

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());

        MvcResult productResult = mockMvc.perform(post("/api/v1/products")
                        .session(adminSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"한정 상품","description":"한정 수량 상품","price":10000,"categoryCode":"STATIONERY","initialStock":10}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long productId = objectMapper.readTree(productResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders")
                        .session(memberSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientName":"수령인","deliveryAddress":"서울시 강남구 테헤란로 1","contactPhoneNumber":"010-1234-5678","deliveryRequest":"문 앞에 놓아주세요","items":[{"productId":%d,"quantity":3}]}
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.totalAmount").value(30000))
                .andExpect(jsonPath("$.recipientName").value("수령인"))
                .andExpect(jsonPath("$.deliveryAddress").value("서울시 강남구 테헤란로 1"))
                .andExpect(jsonPath("$.contactPhoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.deliveryRequest").value("문 앞에 놓아주세요"))
                .andReturn();
        long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/inventories/{productId}", productId)
                        .session(memberSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(7));

        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", orderId)
                        .session(memberSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/v1/inventories/{productId}", productId)
                        .session(memberSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void 배송지_없이_주문을_생성할_수_없다() throws Exception {
        MockHttpSession memberSession = signUpAndLogin("delivery-member@example.com", "회원");

        mockMvc.perform(post("/api/v1/orders")
                        .session(memberSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientName":"수령인","contactPhoneNumber":"010-1234-5678","items":[{"productId":1,"quantity":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void 연락처_없이_주문을_생성할_수_없다() throws Exception {
        MockHttpSession memberSession = signUpAndLogin("phone-member@example.com", "회원");

        mockMvc.perform(post("/api/v1/orders")
                        .session(memberSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientName":"수령인","deliveryAddress":"서울시 강남구 테헤란로 1","items":[{"productId":1,"quantity":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void 역할에_따라_상품_재고_주문_생성_권한을_분리한다() throws Exception {
        MockHttpSession adminSession = createAdminAndLogin("admin@example.com", "관리자");
        MockHttpSession memberSession = signUpAndLogin("role-member@example.com", "회원");

        mockMvc.perform(post("/api/v1/products")
                        .session(memberSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"권한 상품","description":"권한 테스트 상품","price":10000,"categoryCode":"STATIONERY","initialStock":10}
                                """))
                .andExpect(status().isForbidden());

        MvcResult productResult = mockMvc.perform(post("/api/v1/products")
                        .session(adminSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"권한 상품","description":"권한 테스트 상품","price":10000,"categoryCode":"STATIONERY","initialStock":10}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long productId = objectMapper.readTree(productResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/v1/inventories/{productId}", productId)
                        .session(memberSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":20}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/inventories/{productId}", productId)
                        .session(adminSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":20}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20));

        mockMvc.perform(post("/api/v1/orders")
                        .session(adminSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientName":"수령인","deliveryAddress":"서울시 강남구 테헤란로 1","contactPhoneNumber":"010-1234-5678","items":[{"productId":%d,"quantity":1}]}
                                """.formatted(productId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/orders")
                        .session(memberSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientName":"수령인","deliveryAddress":"서울시 강남구 테헤란로 1","contactPhoneNumber":"010-1234-5678","items":[{"productId":%d,"quantity":1}]}
                                """.formatted(productId)))
                .andExpect(status().isCreated());
    }

    @Test
    void 관리자는_상품_정보와_판매_상태를_수정할_수_있다() throws Exception {
        MockHttpSession adminSession = createAdminAndLogin("product-admin@example.com", "관리자");

        MvcResult productResult = mockMvc.perform(post("/api/v1/products")
                        .session(adminSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"기존 상품","description":"기존 설명","price":10000,"categoryCode":"STATIONERY","initialStock":10}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long productId = objectMapper.readTree(productResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/v1/products/{productId}", productId)
                        .session(adminSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"수정 상품","description":"수정 설명","price":12000,"categoryCode":"BOOK","active":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정 상품"))
                .andExpect(jsonPath("$.description").value("수정 설명"))
                .andExpect(jsonPath("$.price").value(12000))
                .andExpect(jsonPath("$.categoryCode").value("BOOK"))
                .andExpect(jsonPath("$.categoryName").value("도서"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void 공개_회원가입은_MEMBER로_등록한다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"public-member@example.com","password":"password123","name":"회원"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    private MockHttpSession signUpAndLogin(String email, String name) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password123","name":"%s"}
                                """.formatted(email, name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("MEMBER"));

        return login(email);
    }

    private MockHttpSession createAdminAndLogin(String email, String name) throws Exception {
        memberRepository.save(new Member(email, passwordEncoder.encode("password123"), name, MemberRole.ADMIN));
        return login(email);
    }

    private MockHttpSession login(String email) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }
}
