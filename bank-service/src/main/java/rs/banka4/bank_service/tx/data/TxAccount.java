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
    long routingNumber();

    @JsonTypeName("PERSON")
    public record Person(ForeignBankId id) implements TxAccount {
        public long routingNumber() {
            return id.routingNumber();
        }
    }

    @JsonTypeName("ACCOUNT")
    public record Account(String num) implements TxAccount {
        public long routingNumber() {
            return Long.valueOf(num.substring(0, 3));
        }
    }

    @JsonTypeName("OPTION")
    public record Option(ForeignBankId id) implements TxAccount {
        public long routingNumber() {
            return id.routingNumber();
        }
    }

    public record MemoryHole() implements TxAccount {
        public long routingNumber() {
            return ForeignBankId.OUR_ROUTING_NUMBER;
        }
    }
}
