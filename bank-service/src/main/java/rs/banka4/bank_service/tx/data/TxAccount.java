package rs.banka4.bank_service.tx.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = TxAccount.Person.class),
    @JsonSubTypes.Type(value = TxAccount.Account.class),
    @JsonSubTypes.Type(value = TxAccount.Option.class)
})
public sealed interface TxAccount {
    @JsonTypeName("PERSON")
    public record Person(ForeignBankId id) implements TxAccount {
    }

    @JsonTypeName("ACCOUNT")
    public record Account(String num) implements TxAccount {
    }

    @JsonTypeName("OPTION")
    public record Option(ForeignBankId id) implements TxAccount {
    }
}
