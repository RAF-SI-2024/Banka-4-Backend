package rs.banka4.user_service.domain.loan.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanMapper {

    LoanMapper INSTANCE = Mappers.getMapper(LoanMapper.class);

    LoanInformationDto toDto(Loan loan);

}
