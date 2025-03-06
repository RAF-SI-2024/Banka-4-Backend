package rs.banka4.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rs.banka4.user_service.dto.TransactionDto;
import rs.banka4.user_service.models.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "fromAccount.accountNumber", target = "fromAccount")
    @Mapping(source = "toAccount.accountNumber", target = "toAccount")
    @Mapping(source = "from.amount", target = "fromAmount")
    @Mapping(source = "from.currency.code", target = "fromCurrency")
    @Mapping(source = "to.amount", target = "toAmount")
    @Mapping(source = "to.currency.code", target = "toCurrency")
    @Mapping(source = "fee.amount", target = "feeAmount")
    @Mapping(source = "fee.currency.code", target = "feeCurrency")
    TransactionDto toDto(Transaction transaction);
}