package rs.banka4.user_service.runners;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;

import java.time.LocalDate;

@Profile({"dev"})
@Component
public class TestDataRunner implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataRunner(ClientRepository clientRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (employeeRepository.count() == 0) {
            Employee employee = new Employee()
                    .withFirstName("John")
                    .withLastName("Doe")
                    .withDateOfBirth(LocalDate.of(1990, 1, 1))
                    .withGender("Male")
                    .withEmail("john.doe@example.com")
                    .withPhone("123-456-7890")
                    .withAddress("123 Main St")
                    .withPassword(passwordEncoder.encode("password"))
                    .withUsername("johndoe")
                    .withPosition("Developer")
                    .withDepartment("IT")
                    .withEnabled(true)
                    .withPermissionBits(0L);

            employeeRepository.save(employee);
        }
    }
}
