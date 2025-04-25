package rs.banka4.bank_service.unit.tx;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.support.TransactionTemplate;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.account.db.AccountType;
import rs.banka4.bank_service.domain.user.client.db.Client;
import rs.banka4.bank_service.domain.user.employee.db.Employee;
import rs.banka4.bank_service.integration.generator.UserGenerator;
import rs.banka4.bank_service.repositories.AccountRepository;
import rs.banka4.bank_service.tx.data.DoubleEntryTransaction;
import rs.banka4.bank_service.tx.data.NoVoteReason;
import rs.banka4.bank_service.tx.data.Posting;
import rs.banka4.bank_service.tx.data.TxAccount;
import rs.banka4.bank_service.tx.data.TxAsset;
import rs.banka4.bank_service.tx.errors.TxLocalPartVotedNo;
import rs.banka4.bank_service.tx.executor.InterbankTxExecutor;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.testlib.integration.DbEnabledTest;

@SpringBootTest
@DbEnabledTest
public class InterbankTxExecutorTests {
    @MockitoBean
    TaskScheduler taskScheduler;

    @Autowired
    InterbankTxExecutor executor;

    @Autowired
    UserGenerator userGen;

    @Autowired
    AccountRepository accRepo;

    @Autowired
    TransactionTemplate txTemplate;

    private String getAccountNumber(int userNr, CurrencyCode curr) {
        return "4440001000%03d%03d520".formatted(userNr, curr.ordinal());
    }

    private Account getAccount(int userNr, CurrencyCode curr) {
        return accRepo.findAccountByAccountNumber(getAccountNumber(userNr, curr))
            .orElseThrow();
    }

    Employee author;
    List<Client> users;

    private static final BigDecimal BASELINE_BALANCE = new BigDecimal(1e9);

    @BeforeEach
    void beforeEach() {
        author =
            userGen.createEmployee(
                x -> x.id(UUID.fromString("771648e8-60bf-4ed4-a2ba-84950e8025db"))
                    .email("emp1@t.co")
            );
        users =
            List.of(
                userGen.createClient(
                    x -> x.id(UUID.fromString("be4f05ae-678e-4b39-a493-e1bd3d48280c"))
                        .email("foo1@t.co")
                ),
                userGen.createClient(
                    x -> x.id(UUID.fromString("7c71c202-46cf-4491-a5a1-f457a36df516"))
                        .email("foo2@t.co")
                )
            );

        for (final var cc : CurrencyCode.values()) {
            for (int user = 0; user < users.size(); user++) {
                final var acc = getAccountNumber(user, cc);
                accRepo.save(
                    new Account(
                        UUID.randomUUID(),
                        acc,
                        BASELINE_BALANCE,
                        BASELINE_BALANCE,
                        BigDecimal.ZERO,
                        LocalDate.now(),
                        LocalDate.now()
                            .plusYears(20),
                        true,
                        AccountType.STANDARD,
                        BASELINE_BALANCE,
                        BASELINE_BALANCE,
                        author,
                        users.get(user),
                        null,
                        cc
                    )
                );
            }
        }
        accRepo.flush();
    }

    @EnumSource(CurrencyCode.class)
    @ParameterizedTest
    void test_immediate_transfer_money_into_user_happy(CurrencyCode cc) {
        executor.submitImmediateTx(
            new DoubleEntryTransaction(
                List.of(
                    new Posting(
                        new TxAccount.MemoryHole(),
                        BigDecimal.TEN.negate(),
                        new TxAsset.Monas(cc)
                    ),
                    new Posting(
                        new TxAccount.Account(getAccountNumber(0, cc)),
                        BigDecimal.TEN,
                        new TxAsset.Monas(cc)
                    )
                ),
                "foo",
                null
            )
        );

        assertThat(getAccount(0, cc).getBalance()).isEqualByComparingTo(
            BASELINE_BALANCE.add(BigDecimal.TEN)
        );
        assertThat(getAccount(0, cc).getAvailableBalance()).isEqualByComparingTo(
            BASELINE_BALANCE.add(BigDecimal.TEN)
        );
    }

    @EnumSource(CurrencyCode.class)
    @ParameterizedTest
    void test_immediate_transfer_money_from_user_happy(CurrencyCode cc) {
        executor.submitImmediateTx(
            new DoubleEntryTransaction(
                List.of(
                    new Posting(new TxAccount.MemoryHole(), BigDecimal.TEN, new TxAsset.Monas(cc)),
                    new Posting(
                        new TxAccount.Account(getAccountNumber(0, cc)),
                        BigDecimal.TEN.negate(),
                        new TxAsset.Monas(cc)
                    )
                ),
                "foo",
                null
            )
        );

        assertThat(getAccount(0, cc).getBalance()).isEqualByComparingTo(
            BASELINE_BALANCE.subtract(BigDecimal.TEN)
        );
        assertThat(getAccount(0, cc).getAvailableBalance()).isEqualByComparingTo(
            BASELINE_BALANCE.subtract(BigDecimal.TEN)
        );
    }

    @Test
    void test_immediate_transfer_money_from_user_broke() {
        final var moreThanBaseline = BASELINE_BALANCE.add(new BigDecimal(100));

        final var insufficientPosting =
            new Posting(
                new TxAccount.Account(getAccountNumber(0, CurrencyCode.GBP)),
                moreThanBaseline.negate(),
                new TxAsset.Monas(CurrencyCode.GBP)
            );
        assertThatExceptionOfType(TxLocalPartVotedNo.class).isThrownBy(
            () -> txTemplate.executeWithoutResult(
                s -> executor.submitImmediateTx(
                    new DoubleEntryTransaction(
                        List.of(
                            new Posting(
                                new TxAccount.MemoryHole(),
                                moreThanBaseline,
                                new TxAsset.Monas(CurrencyCode.GBP)
                            ),
                            insufficientPosting
                        ),
                        "foo",
                        null
                    )
                )
            )
        )
            .extracting(TxLocalPartVotedNo::getReasons, as(LIST))
            .singleElement()
            .asInstanceOf(type(NoVoteReason.InsufficientAsset.class))
            .extracting("posting", type(Posting.class))
            .isEqualTo(insufficientPosting);

        assertThat(getAccount(0, CurrencyCode.GBP).getBalance()).isEqualByComparingTo(
            BASELINE_BALANCE
        );
        assertThat(getAccount(0, CurrencyCode.GBP).getAvailableBalance()).isEqualByComparingTo(
            BASELINE_BALANCE
        );
    }

    @Test
    void test_immediate_transfer_money_from_user_wrong_currency() {
        final var moreThanBaseline = BASELINE_BALANCE.add(new BigDecimal(100));

        final var insufficientPosting =
            new Posting(
                new TxAccount.Account(getAccountNumber(0, CurrencyCode.GBP)),
                moreThanBaseline.negate(),
                new TxAsset.Monas(CurrencyCode.RSD)
            );
        assertThatExceptionOfType(TxLocalPartVotedNo.class).isThrownBy(
            () -> txTemplate.executeWithoutResult(
                s -> executor.submitImmediateTx(
                    new DoubleEntryTransaction(
                        List.of(
                            new Posting(
                                new TxAccount.MemoryHole(),
                                moreThanBaseline,
                                new TxAsset.Monas(CurrencyCode.RSD)
                            ),
                            insufficientPosting
                        ),
                        "foo",
                        null
                    )
                )
            )
        )
            .extracting(TxLocalPartVotedNo::getReasons, as(LIST))
            .singleElement()
            .asInstanceOf(type(NoVoteReason.UnacceptableAsset.class))
            .extracting("posting", type(Posting.class))
            .isEqualTo(insufficientPosting);

        assertThat(getAccount(0, CurrencyCode.GBP).getBalance()).isEqualByComparingTo(
            BASELINE_BALANCE
        );
        assertThat(getAccount(0, CurrencyCode.GBP).getAvailableBalance()).isEqualByComparingTo(
            BASELINE_BALANCE
        );
    }

}
