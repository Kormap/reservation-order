package com.reservation.order.domain;

import com.reservation.common.exception.BusinessException;
import com.reservation.common.exception.ErrorCode;
import com.reservation.member.domain.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "reservation_orders")
public class ReservationOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationOrderStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 255)
    private String deliveryAddress;

    @Column(length = 20)
    private String contactPhoneNumber;

    @Column(length = 100)
    private String recipientName;

    @Column(length = 500)
    private String deliveryRequest;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant cancelledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationOrderItem> items = new ArrayList<>();

    protected ReservationOrder() {
    }

    public ReservationOrder(Member member, String recipientName, String deliveryAddress, String contactPhoneNumber,
                            String deliveryRequest) {
        this.member = member;
        this.status = ReservationOrderStatus.RESERVED;
        this.totalAmount = BigDecimal.ZERO;
        this.recipientName = recipientName;
        this.deliveryAddress = deliveryAddress;
        this.contactPhoneNumber = contactPhoneNumber;
        this.deliveryRequest = deliveryRequest;
        this.createdAt = Instant.now();
    }

    public void addItem(Long productId, String productName, BigDecimal unitPrice, int quantity) {
        ReservationOrderItem item = new ReservationOrderItem(this, productId, productName, unitPrice, quantity);
        items.add(item);
        totalAmount = totalAmount.add(item.getLineAmount());
    }

    public void cancel() {
        if (status == ReservationOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        status = ReservationOrderStatus.CANCELLED;
        cancelledAt = Instant.now();
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public ReservationOrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getContactPhoneNumber() { return contactPhoneNumber; }
    public String getRecipientName() { return recipientName; }
    public String getDeliveryRequest() { return deliveryRequest; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public List<ReservationOrderItem> getItems() { return Collections.unmodifiableList(items); }
}
