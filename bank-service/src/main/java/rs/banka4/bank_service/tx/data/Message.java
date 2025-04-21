package rs.banka4.bank_service.tx.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "messageType"
)
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = Message.NewTx.class),
    @JsonSubTypes.Type(value = Message.CommitTx.class),
    @JsonSubTypes.Type(value = Message.RollbackTx.class)
})
public sealed interface Message {
    @JsonTypeName("NEW_TX")
    public record NewTx(
        IdempotenceKey idempotenceKey,
        Transaction message
    ) implements Message {
    }

    @JsonTypeName("COMMIT_TX")
    public record CommitTx(
        IdempotenceKey idempotenceKey,
        CommitTransaction message
    ) implements Message {
    }

    @JsonTypeName("ROLLBACK_TX")
    public record RollbackTx(
        IdempotenceKey idempotenceKey,
        RollbackTransaction message
    ) implements Message {
    }
}
