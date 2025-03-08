package rs.banka4.user_service.domain.user.employee.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.exceptions.PrivilegeDoesNotExist;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.Privilege;

import java.util.stream.Collectors;

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
        employee.setPrivileges(
                dto.privilege().stream()
                        .map(privilege -> {
                            try {
                                return Privilege.valueOf(String.valueOf(privilege));
                            } catch (IllegalArgumentException e) {
                                throw new PrivilegeDoesNotExist(privilege.toString());
                            }
                        })
                        .collect(Collectors.toSet())
        );        employee.setPosition(dto.position());
        employee.setDepartment(dto.department());
        employee.setEnabled(dto.active());
        employee.setActive(dto.active());
        return employee;
    }

    public EmployeeDto toDto(Employee employee){
        return new EmployeeDto(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDateOfBirth(),
                employee.getGender(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getAddress(),
                employee.getUsername(),
                employee.getPosition(),
                employee.getDepartment(),
                employee.isActive()
        );
    }
}
