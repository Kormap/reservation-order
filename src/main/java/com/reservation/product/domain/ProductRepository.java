package com.reservation.product.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            select p from Product p
            where (:categoryCode is null or p.categoryCode = :categoryCode)
              and (:productName is null or lower(p.name) like lower(concat('%', :productName, '%')))
            order by p.id
            """)
    List<Product> search(@Param("categoryCode") String categoryCode, @Param("productName") String productName);
}
