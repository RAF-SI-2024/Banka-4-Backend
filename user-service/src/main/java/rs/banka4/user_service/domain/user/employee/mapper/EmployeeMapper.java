package rs.banka4.user_service.domain.user.employee.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import rs.banka4.rafeisen.common.dto.EmployeeResponseDto;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    Employee toEntity(CreateEmployeeDto dto);

    EmployeeDto toDto(Employee employee);

    EmployeeResponseDto toResponseDto(Employee employee);

    @Mapping(
        target = "phone",
        source = "phoneNumber"
    )
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdate(@MappingTarget Employee employee, UpdateEmployeeDto dto);

    @AfterMapping
    default void mapPrivileges(CreateEmployeeDto dto, @MappingTarget Employee employee) {
        if (dto.privilege() != null) {
            employee.setPrivileges(dto.privilege());
        }
    }
}
