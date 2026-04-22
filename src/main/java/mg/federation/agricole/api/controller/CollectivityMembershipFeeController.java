package mg.federation.agricole.api.controller;

import mg.federation.agricole.api.dto.MembershipFee;
import mg.federation.agricole.api.service.MembershipFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CollectivityMembershipFeeController {

    private final MembershipFeeService membershipFeeService;

    public CollectivityMembershipFeeController(MembershipFeeService membershipFeeService) {
        this.membershipFeeService = membershipFeeService;
    }

    @GetMapping("/collectivities/{id}/membershipFees")
    public ResponseEntity<List<MembershipFee>> getMembershipFees(@PathVariable String id) {
        List<MembershipFee> fees = membershipFeeService.getMembershipFees(id);
        return ResponseEntity.ok(fees);
    }
}
