package rs.banka4.user_service.domain.user.employee.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeResponseDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.db.Employee;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    Employee toEntity(CreateEmployeeDto dto);
    EmployeeDto toDto(Employee employee);
    EmployeeResponseDto toResponseDto(Employee employee);

    void fromUpdate(@MappingTarget Employee employee, UpdateEmployeeDto dto);

    @AfterMapping
    default void mapPrivileges(CreateEmployeeDto dto, @MappingTarget Employee employee) {
        if (dto.privilege() != null) {
            employee.setPrivileges(dto.privilege());
        }
    }
}
