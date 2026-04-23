package mg.federation.agricole.api.controller;

import mg.federation.agricole.api.dto.CreateMemberPayment;
import mg.federation.agricole.api.dto.MemberPayment;
import mg.federation.agricole.api.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MemberPaymentController {

    private final PaymentService paymentService;

    public MemberPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/members/{id}/payments")
    public ResponseEntity<List<MemberPayment>> createPayments(
            @PathVariable String id,
            @RequestBody List<CreateMemberPayment> payments) {
        List<MemberPayment> createdPayments = paymentService.createMemberPayments(id, payments);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayments);
    }
}
