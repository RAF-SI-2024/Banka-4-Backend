package rs.banka4.bank_service.tx.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = TxAsset.Monas.class),
    @JsonSubTypes.Type(value = TxAsset.Stock.class),
    @JsonSubTypes.Type(value = TxAsset.Option.class)
})
public sealed interface TxAsset {
    @JsonTypeName("MONAS")
    public record Monas(MonetaryAsset asset) implements TxAsset {
        public Monas(CurrencyCode cc) {
            this(new MonetaryAsset(cc));
        }
    }

    @JsonTypeName("STOCK")
    public record Stock(StockDescription asset) implements TxAsset {
        public Stock(String ticker) {
            this(new StockDescription(ticker));
        }
    }

    @JsonTypeName("OPTION")
    public record Option(OptionDescription asset) implements TxAsset {
    }
}
