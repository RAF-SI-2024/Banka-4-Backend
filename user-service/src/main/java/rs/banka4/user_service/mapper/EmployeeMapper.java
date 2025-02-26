package rs.banka4.user_service.mapper;

import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.banka4.user_service.dto.CreateEmployeeDto;
import rs.banka4.user_service.dto.EmployeeUpdateDto;
import rs.banka4.user_service.models.Employee;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {

    Employee toEntity(EmployeeUpdateDto dto);

    EmployeeUpdateDto toDto(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeFromDto(EmployeeUpdateDto dto, @MappingTarget Employee employee,@Context PasswordEncoder passwordEncoder);

    @AfterMapping
    default void afterUpdate(@MappingTarget Employee employee, EmployeeUpdateDto dto,@Context PasswordEncoder passwordEncoder) {
        if (dto.getPrivilege() != null) {
            employee.setPrivileges(dto.getPrivilege());
        }
        if (dto.getPassword() != null) {
            if(!passwordEncoder.matches(dto.getPassword(), employee.getPassword())){
                employee.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        }
    }
}
