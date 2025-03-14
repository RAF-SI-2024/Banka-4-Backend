package rs.banka4.user_service.domain.transaction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.transaction.db.Transaction;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;

@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(
        source = "fromAccount.accountNumber",
        target = "fromAccount"
    )
    @Mapping(
        source = "toAccount.accountNumber",
        target = "toAccount"
    )
    @Mapping(
        source = "from.amount",
        target = "fromAmount"
    )
    @Mapping(
        source = "from.currency.code",
        target = "fromCurrency"
    )
    @Mapping(
        source = "to.amount",
        target = "toAmount"
    )
    @Mapping(
        source = "to.currency.code",
        target = "toCurrency"
    )
    @Mapping(
        source = "fee.amount",
        target = "feeAmount"
    )
    @Mapping(
        source = "fee.currency.code",
        target = "feeCurrency"
    )
    TransactionDto toDto(Transaction transaction);
}
