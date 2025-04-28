package rs.banka4.bank_service.tx.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "vote"
)
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = TransactionVote.Yes.class),
    @JsonSubTypes.Type(value = TransactionVote.No.class)
})
public sealed interface TransactionVote {
    @JsonTypeName("YES")
    public record Yes() implements TransactionVote {
    }

    @JsonTypeName("NO")
    public record No(List<NoVoteReason> reasons) implements TransactionVote {
    }
}
