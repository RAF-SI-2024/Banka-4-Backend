package rs.banka4.bank_service.tx.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "reason"
)
public sealed interface NoVoteReason {
    @JsonTypeName("UNBALANCED_TX")
    public record UnbalancedTx() implements NoVoteReason {
    }

    @JsonTypeName("NO_SUCH_ACCOUNT")
    public record NoSuchAccount(Posting posting) implements NoVoteReason {
    }

    @JsonTypeName("NO_SUCH_ASSET")
    public record NoSuchAsset(Posting posting) implements NoVoteReason {
    }

    @JsonTypeName("INSUFFICIENT_ASSET")
    public record InsufficientAsset(Posting posting) implements NoVoteReason {
    }

    @JsonTypeName("OPTION_AMOUNT_INCORRECT")
    public record OptionAmountIncorrect(Posting posting) implements NoVoteReason {
    }

    @JsonTypeName("OPTION_USED_OR_EXPIRED")
    public record OptionUsedOrExpired(Posting posting) implements NoVoteReason {
    }

    @JsonTypeName("UNACCEPTABLE_ASSET")
    public record UnacceptableAsset(Posting posting) implements NoVoteReason {
    }
}
