package rs.banka4.bank_service.unit.tx;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.Test;
import rs.banka4.bank_service.tx.TxUtils;
import rs.banka4.bank_service.tx.data.DoubleEntryTransaction;
import rs.banka4.bank_service.tx.data.Posting;
import rs.banka4.bank_service.tx.data.TxAccount;
import rs.banka4.bank_service.tx.data.TxAsset;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

public class TxUtilsTests {
    @Test
    void test_isTxBalanced_empty() {
        assertThat(TxUtils.isTxBalanced(new DoubleEntryTransaction(List.of(), null, null)))
            .isTrue();
    }

    @Test
    void test_isTxBalanced_basic() {
        assertThat(
            TxUtils.isTxBalanced(
                new DoubleEntryTransaction(
                    List.of(
                        new Posting(
                            new TxAccount.MemoryHole(),
                            new BigDecimal(5),
                            new TxAsset.Monas(CurrencyCode.CAD)
                        ),
                        new Posting(
                            new TxAccount.MemoryHole(),
                            new BigDecimal(5).negate(),
                            new TxAsset.Monas(CurrencyCode.CAD)
                        )
                    ),
                    null,
                    null
                )
            )
        ).isTrue();
    }

    @Test
    void test_isTxBalanced_considering_bigdecimal_equals() {
        assertThat(
            TxUtils.isTxBalanced(
                new DoubleEntryTransaction(
                    List.of(
                        new Posting(
                            new TxAccount.MemoryHole(),
                            new BigDecimal(5).setScale(15, RoundingMode.UNNECESSARY),
                            new TxAsset.Monas(CurrencyCode.CAD)
                        ),
                        new Posting(
                            new TxAccount.MemoryHole(),
                            new BigDecimal(5).negate(),
                            new TxAsset.Monas(CurrencyCode.CAD)
                        )
                    ),
                    null,
                    null
                )
            )
        ).isTrue();
    }

    @Test
    void test_isTxBalanced_disbalance() {
        assertThat(
            TxUtils.isTxBalanced(
                new DoubleEntryTransaction(
                    List.of(
                        new Posting(
                            new TxAccount.MemoryHole(),
                            new BigDecimal(5),
                            new TxAsset.Monas(CurrencyCode.CAD)
                        ),
                        new Posting(
                            new TxAccount.MemoryHole(),
                            new BigDecimal(6).negate(),
                            new TxAsset.Monas(CurrencyCode.CAD)
                        )
                    ),
                    null,
                    null
                )
            )
        ).isFalse();
    }
}
