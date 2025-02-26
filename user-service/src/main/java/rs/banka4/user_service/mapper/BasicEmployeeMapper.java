package rs.banka4.user_service.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.dto.CreateEmployeeDto;
import rs.banka4.user_service.models.Employee;

@Component
public class BasicEmployeeMapper {
    private final PasswordEncoder passwordEncoder;

    public BasicEmployeeMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Employee toEntity(CreateEmployeeDto dto) {
        Employee employee = new Employee();
        employee.setFirstName(dto.firstName());
        employee.setLastName(dto.lastName());
        employee.setUsername(dto.username());
        employee.setDateOfBirth(dto.dateOfBirth());
        employee.setGender(dto.gender());
        employee.setEmail(dto.email());
        employee.setPhone(dto.phone());
        employee.setAddress(dto.address());
        employee.setPassword(passwordEncoder.encode(dto.password()));
        employee.setPrivileges(dto.privilege());
        employee.setPosition(dto.position());
        employee.setDepartment(dto.department());
        employee.setEnabled(false);
        return employee;
    }
}
