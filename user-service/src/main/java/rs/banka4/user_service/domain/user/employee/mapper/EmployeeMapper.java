package rs.banka4.user_service.domain.user.employee.mapper;

import org.mapstruct.*;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeResponseDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.exceptions.PrivilegeDoesNotExist;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.Privilege;

import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {
    Employee toEntity(UpdateEmployeeDto dto);

    UpdateEmployeeDto toDto(Employee employee);

    Employee toEntity(EmployeeResponseDto employeeResponseDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeFromDto(UpdateEmployeeDto dto, @MappingTarget Employee employee);

    @AfterMapping
    default void afterUpdate(@MappingTarget Employee employee, UpdateEmployeeDto dto) {
        if (dto.privilege() != null) {
            employee.setPrivileges(
                    dto.privilege().stream()
                            .map(privilege -> {
                                try {
                                    return Privilege.valueOf(privilege);
                                } catch (IllegalArgumentException e) {
                                    throw new PrivilegeDoesNotExist(privilege);
                                }
                            })
                            .collect(Collectors.toSet())
            );
        }
        employee.setActive(dto.active());
    }
}
