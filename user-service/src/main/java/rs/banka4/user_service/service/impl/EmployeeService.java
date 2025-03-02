package rs.banka4.user_service.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.requests.CreateEmployeeRequest;
import rs.banka4.user_service.exceptions.DuplicateUsername;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.EmployeeAbs;

@Service
public class EmployeeService implements EmployeeAbs {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Employee createEmployee(CreateEmployeeRequest dto) {
        if(employeeRepository.existsByEmail(dto.email()) || employeeRepository.existsByUsername(dto.email())){
            throw new DuplicateUsername("There is already an employee with that email");
        }
        return null;
    }
}
