package rs.banka4.user_service.mapper;

import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.banka4.user_service.dto.CreateEmployeeDto;
import rs.banka4.user_service.models.Employee;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {

}
