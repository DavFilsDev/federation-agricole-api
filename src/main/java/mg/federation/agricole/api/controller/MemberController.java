// controller/MemberController.java
package mg.federation.agricole.api.controller;

import mg.federation.agricole.api.dto.CreateMember;
import mg.federation.agricole.api.dto.Member;
import mg.federation.agricole.api.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/members")
    public ResponseEntity<List<Member>> createMembers(@RequestBody List<CreateMember> createList) {
        List<Member> result = memberService.createMembers(createList);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}