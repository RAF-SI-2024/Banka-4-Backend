package rs.banka4.user_service.runners;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.models.*;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.CurrencyRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Profile({"dev"})
@Component
@RequiredArgsConstructor
public class TestDataRunner implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository;

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
        Employee newEmployeeMarko = employeeRepository.findByUsername(newUsername)
                .orElseGet(() -> {
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
                    return employeeRepository.save(newUser);
                });

        String newClientEmail = "mkarisik@raf.rs";
        String newClientEmailSecond = "iatanas@raf.rs";

        Client newClientMehmedalija = clientRepository.findByEmail(newClientEmail)
                .orElseGet(() -> {
                    Client newClient = Client.builder()
                            .firstName("Mehmedalija")
                            .lastName("Karisik")
                            .dateOfBirth(LocalDate.of(2001, 1, 1))
                            .gender("Male")
                            .email(newClientEmail)
                            .phone("381062323929292")
                            .address("456 Elm St")
                            .password(passwordEncoder.encode("qwerty123"))
                            .accounts(Set.of())
                            .contacts(Set.of())
                            .enabled(true)
                            .build();
                    return clientRepository.save(newClient);
                });

        if (!clientRepository.existsByEmail(newClientEmailSecond)) {
            Client newClient = Client.builder()
                    .firstName("Ivana")
                    .lastName("Atanasijevic")
                    .dateOfBirth(LocalDate.of(2003, 1, 5))
                    .gender("Female")
                    .email(newClientEmailSecond)
                    .phone("3810629292")
                    .address("456 Elm St 2")
                    .password(passwordEncoder.encode("password123"))
                    .accounts(Set.of())
                    .contacts(Set.of())
                    .enabled(true)
                    .build();

            clientRepository.save(newClient);
        }


    }
}