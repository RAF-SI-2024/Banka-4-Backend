package rs.banka4.user_service.mapper;

import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.banka4.user_service.dto.EmployeeUpdateDto;
import rs.banka4.user_service.exceptions.PrivilegeDoesNotExist;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.Privilege;

import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {
    Employee toEntity(EmployeeUpdateDto dto);

    EmployeeUpdateDto toDto(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeFromDto(EmployeeUpdateDto dto, @MappingTarget Employee employee,@Context PasswordEncoder passwordEncoder);

    @AfterMapping
    default void afterUpdate(@MappingTarget Employee employee, EmployeeUpdateDto dto,@Context PasswordEncoder passwordEncoder) {
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
        if (dto.password() != null) {
            if(!passwordEncoder.matches(dto.password(), employee.getPassword())){
                employee.setPassword(passwordEncoder.encode(dto.password()));
            }
        }
    }
}
