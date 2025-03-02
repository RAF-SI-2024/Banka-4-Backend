package rs.banka4.user_service.runners;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.User;
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
            Employee employee = Employee.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender("Male")
                    .email("john.doe@example.com")
                    .phone("123-456-7890")
                    .address("123 Main St")
                    .password(passwordEncoder.encode("password"))
                    .username("johndoe")
                    .position("Developer")
                    .department("IT")
                    .active(true)
                    .enabled(true)
                    .permissionBits(1L)
                    .build();

            employeeRepository.save(employee);
        }

        String newUsername = "marko";
        if (!employeeRepository.existsByUsername(newUsername)) {
            Employee newUser = Employee.builder()
                    .firstName("Marko")
                    .lastName("Markovic")
                    .dateOfBirth(LocalDate.of(2001, 1, 1))
                    .gender("Male")
                    .email("markovicmarko@example.com")
                    .phone("987-654-3210")
                    .address("456 Elm St")
                    .password(passwordEncoder.encode("securepassword"))
                    .username(newUsername)
                    .position("Software Engineer")
                    .department("Development")
                    .active(true)
                    .enabled(true)
                    .permissionBits(1L)
                    .build();

            employeeRepository.save(newUser);
        }
    }
}
