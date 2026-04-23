package mg.federation.agricole.api.service;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.CollectivityTransaction;
import mg.federation.agricole.api.dto.FinancialAccount;
import mg.federation.agricole.api.dto.Member;
import mg.federation.agricole.api.entity.MemberEntity;
import mg.federation.agricole.api.entity.TransactionEntity;
import mg.federation.agricole.api.exception.BusinessRuleException;
import mg.federation.agricole.api.exception.ResourceNotFoundException;
import mg.federation.agricole.api.repository.*;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final DataSource dataSource;
    private final CollectivityRepository collectivityRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final MemberRepository memberRepository;

    public TransactionService(DataSource dataSource,
                              CollectivityRepository collectivityRepository,
                              TransactionRepository transactionRepository,
                              FinancialAccountRepository financialAccountRepository,
                              MemberRepository memberRepository) {
        this.dataSource = dataSource;
        this.collectivityRepository = collectivityRepository;
        this.transactionRepository = transactionRepository;
        this.financialAccountRepository = financialAccountRepository;
        this.memberRepository = memberRepository;
    }

    public List<CollectivityTransaction> getCollectivityTransactions(String collectivityIdStr, LocalDate from, LocalDate to) {
        // Validation des dates
        if (from == null || to == null) {
            throw new BusinessRuleException("Query parameters 'from' and 'to' are mandatory");
        }

        if (from.isAfter(to)) {
            throw new BusinessRuleException("'from' date cannot be after 'to' date");
        }

        try (Connection conn = dataSource.getConnection()) {
            Long collectivityId = Long.parseLong(collectivityIdStr);

            // Vérifier que la collectivité existe
            if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityIdStr);
            }

            // Récupérer les transactions
            List<TransactionEntity> entities = transactionRepository.findByCollectivityIdAndDateRange(conn, collectivityId, from, to);

            // Convertir en DTOs
            List<CollectivityTransaction> transactions = new ArrayList<>();
            for (TransactionEntity entity : entities) {
                CollectivityTransaction dto = new CollectivityTransaction();
                dto.setId(String.valueOf(entity.getId()));
                dto.setCreationDate(entity.getCreationDate());
                dto.setAmount(entity.getAmount());
                dto.setPaymentMode(entity.getPaymentMode());

                // Récupérer le compte crédité
                Optional<FinancialAccount> accountOpt = financialAccountRepository.findById(conn, entity.getAccountCreditedId());
                accountOpt.ifPresent(dto::setAccountCredited);

                // Récupérer le membre débiteur
                Optional<MemberEntity> memberOpt = memberRepository.findById(conn, entity.getMemberId());
                if (memberOpt.isPresent()) {
                    Member memberDto = toMemberDto(memberOpt.get(), null);
                    dto.setMemberDebited(memberDto);
                }

                transactions.add(dto);
            }

            return transactions;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching transactions", e);
        }
    }

    private Member toMemberDto(MemberEntity entity, List<Member> referees) {
        Member dto = new Member();
        dto.setId(String.valueOf(entity.getId()));
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setBirthDate(entity.getBirthDate());
        dto.setGender(mg.federation.agricole.api.dto.Gender.valueOf(entity.getGender()));
        dto.setAddress(entity.getAddress());
        dto.setProfession(entity.getProfession());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        dto.setReferees(referees);
        return dto;
    }
}
