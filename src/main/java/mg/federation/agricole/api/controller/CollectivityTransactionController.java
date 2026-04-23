package mg.federation.agricole.api.controller;

import mg.federation.agricole.api.dto.CollectivityTransaction;
import mg.federation.agricole.api.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class CollectivityTransactionController {

    private final TransactionService transactionService;

    public CollectivityTransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/collectivities/{id}/transactions")
    public ResponseEntity<List<CollectivityTransaction>> getTransactions(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<CollectivityTransaction> transactions = transactionService.getCollectivityTransactions(id, from, to);
        return ResponseEntity.ok(transactions);
    }
}
