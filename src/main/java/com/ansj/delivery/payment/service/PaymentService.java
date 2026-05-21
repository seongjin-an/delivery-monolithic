package com.ansj.delivery.payment.service;

import com.ansj.delivery.common.exception.BusinessException;
import com.ansj.delivery.order.domain.Order;
import com.ansj.delivery.order.domain.OrderStatus;
import com.ansj.delivery.order.repository.OrderRepository;
import com.ansj.delivery.payment.domain.Payment;
import com.ansj.delivery.payment.dto.PaymentRequest;
import com.ansj.delivery.payment.dto.PaymentResponse;
import com.ansj.delivery.payment.pg.PgClient;
import com.ansj.delivery.payment.pg.PgResult;
import com.ansj.delivery.payment.repository.PaymentRepository;
import com.ansj.delivery.user.domain.User;
import com.ansj.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PgClient pgClient;

    @Transactional
    public PaymentResponse pay(UUID customerId, PaymentRequest request) {
        User customer = findUser(customerId);
        Order order = orderRepository.findByIdWithLock(request.orderId())
                .orElseThrow(() -> BusinessException.notFound("주문을 찾을 수 없습니다."));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw BusinessException.forbidden("해당 주문에 대한 권한이 없습니다.");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw BusinessException.badRequest("결제 대기 상태의 주문만 결제할 수 있습니다.");
        }
        if (paymentRepository.findByOrder(order).isPresent()) {
            throw BusinessException.conflict("이미 결제가 진행된 주문입니다.");
        }

        Payment payment = Payment.builder()
                .order(order)
                .customer(customer)
                .amount(order.getTotalAmount())
                .method(request.method())
                .build();
        paymentRepository.save(payment);

        PgResult result = pgClient.pay(order.getTotalAmount(), request.method().name());

        if (result.success()) {
            payment.complete(result.pgTransactionId());
            order.completePayment();
        } else {
            payment.fail();
            throw BusinessException.badRequest("결제에 실패했습니다: " + result.failReason());
        }

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse refund(UUID customerId, UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessException.notFound("결제 정보를 찾을 수 없습니다."));

        if (!payment.getCustomer().getId().equals(customerId)) {
            throw BusinessException.forbidden("해당 결제에 대한 권한이 없습니다.");
        }

        Order order = payment.getOrder();
        if (order.getStatus() != OrderStatus.CANCELLED) {
            throw BusinessException.badRequest("취소된 주문만 환불할 수 있습니다.");
        }

        pgClient.refund(payment.getPgTransactionId());
        payment.refund();

        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByOrder(UUID customerId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.notFound("주문을 찾을 수 없습니다."));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw BusinessException.forbidden("해당 주문에 대한 권한이 없습니다.");
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> BusinessException.notFound("결제 정보를 찾을 수 없습니다."));

        return PaymentResponse.from(payment);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다."));
    }
}
