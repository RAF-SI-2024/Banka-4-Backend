package rs.banka4.bank_service.tx.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

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
    }

    @JsonTypeName("STOCK")
    public record Stock(StockDescription asset) implements TxAsset {
    }

    @JsonTypeName("OPTION")
    public record Option(OptionDescription asset) implements TxAsset {
    }
}
