package mg.federation.agricole.api.service;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.*;
import mg.federation.agricole.api.entity.*;
import mg.federation.agricole.api.exception.BusinessRuleException;
import mg.federation.agricole.api.exception.ResourceNotFoundException;
import mg.federation.agricole.api.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final DataSource dataSource;
    private final MemberRepository memberRepository;
    private final CollectivityRepository collectivityRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipFeeRepository membershipFeeRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final TransactionRepository transactionRepository;

    private static final List<String> VALID_PAYMENT_MODES = List.of("CASH", "MOBILE_BANKING", "BANK_TRANSFER");

    public PaymentService(DataSource dataSource,
                          MemberRepository memberRepository,
                          CollectivityRepository collectivityRepository,
                          MembershipRepository membershipRepository,
                          MembershipFeeRepository membershipFeeRepository,
                          FinancialAccountRepository financialAccountRepository,
                          TransactionRepository transactionRepository) {
        this.dataSource = dataSource;
        this.memberRepository = memberRepository;
        this.collectivityRepository = collectivityRepository;
        this.membershipRepository = membershipRepository;
        this.membershipFeeRepository = membershipFeeRepository;
        this.financialAccountRepository = financialAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<MemberPayment> createMemberPayments(String memberIdStr, List<CreateMemberPayment> payments) {
        if (payments == null || payments.isEmpty()) {
            throw new BusinessRuleException("At least one payment must be provided");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String memberId = memberIdStr;

                MemberEntity member = memberRepository.findById(conn, memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberIdStr));

                String collectivityId = membershipRepository.findCollectivityIdByMemberId(conn, memberId)
                        .orElseThrow(() -> new BusinessRuleException("Member is not affiliated to any collectivity"));

                collectivityRepository.findById(conn, collectivityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Collectivity not found"));

                List<MemberPayment> createdPayments = new ArrayList<>();

                for (CreateMemberPayment payment : payments) {
                    if (payment.getAmount() == null || payment.getAmount() <= 0) {
                        throw new BusinessRuleException("Amount must be positive");
                    }

                    if (payment.getPaymentMode() == null || !VALID_PAYMENT_MODES.contains(payment.getPaymentMode())) {
                        throw new BusinessRuleException("Invalid payment mode. Allowed: CASH, MOBILE_BANKING, BANK_TRANSFER");
                    }

                    String membershipFeeId = null;
                    if (payment.getMembershipFeeIdentifier() != null && !payment.getMembershipFeeIdentifier().isEmpty()) {
                        membershipFeeId = payment.getMembershipFeeIdentifier();
                        Optional<MembershipFeeEntity> feeOpt = membershipFeeRepository.findById(conn, membershipFeeId);
                        if (feeOpt.isEmpty()) {
                            throw new ResourceNotFoundException("Membership fee not found with id: " + payment.getMembershipFeeIdentifier());
                        }
                        MembershipFeeEntity fee = feeOpt.get();
                        if (!fee.getCollectivityId().equals(collectivityId)) {
                            throw new BusinessRuleException("Membership fee does not belong to member's collectivity");
                        }
                        if (!"ACTIVE".equals(fee.getStatus())) {
                            throw new BusinessRuleException("Membership fee is not active");
                        }
                    }

                    String accountId = payment.getAccountCreditedIdentifier();
                    Optional<FinancialAccount> accountOpt = financialAccountRepository.findById(conn, accountId);
                    if (accountOpt.isEmpty()) {
                        throw new ResourceNotFoundException("Financial account not found with id: " + payment.getAccountCreditedIdentifier());
                    }

                    if (!financialAccountRepository.isAccountBelongsToCollectivity(conn, accountId, collectivityId)) {
                        throw new BusinessRuleException("Account does not belong to member's collectivity");
                    }

                    String transactionId = generateTransactionId();

                    TransactionEntity transaction = new TransactionEntity();
                    transaction.setId(transactionId);
                    transaction.setMemberId(memberId);
                    transaction.setCollectivityId(collectivityId);
                    transaction.setAmount(BigDecimal.valueOf(payment.getAmount()));
                    transaction.setPaymentMode(payment.getPaymentMode());
                    transaction.setAccountCreditedId(accountId);
                    transaction.setMembershipFeeId(membershipFeeId);
                    transaction.setCreationDate(LocalDate.now());

                    transactionRepository.insert(conn, transaction);

                    FinancialAccount account = accountOpt.get();
                    BigDecimal currentAmount = account.getAmount();
                    BigDecimal newAmount = currentAmount.add(BigDecimal.valueOf(payment.getAmount()));
                    financialAccountRepository.updateAmount(conn, accountId, newAmount);

                    Optional<FinancialAccount> updatedAccountOpt = financialAccountRepository.findById(conn, accountId);

                    MemberPayment response = new MemberPayment();
                    response.setId(transactionId);
                    response.setAmount(payment.getAmount());
                    response.setPaymentMode(payment.getPaymentMode());
                    updatedAccountOpt.ifPresent(response::setAccountCredited);
                    response.setCreationDate(LocalDate.now());

                    createdPayments.add(response);
                }

                conn.commit();
                return createdPayments;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Database error while creating payments", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error", e);
        }
    }

    private String generateTransactionId() {
        return "tx-" + UUID.randomUUID().toString().substring(0, 8);
    }
}