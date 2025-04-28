package rs.banka4.bank_service.domain.transaction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import rs.banka4.bank_service.domain.transaction.db.Transaction;
import rs.banka4.bank_service.domain.transaction.dtos.TransactionDto;

@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(
        source = "from.amount",
        target = "fromAmount"
    )
    @Mapping(
        source = "from.currency",
        target = "fromCurrency"
    )
    @Mapping(
        source = "to.amount",
        target = "toAmount"
    )
    @Mapping(
        source = "to.currency",
        target = "toCurrency"
    )
    @Mapping(
        source = "fee.amount",
        target = "feeAmount"
    )
    @Mapping(
        source = "fee.currency",
        target = "feeCurrency"
    )
    TransactionDto toDto(Transaction transaction);
}
