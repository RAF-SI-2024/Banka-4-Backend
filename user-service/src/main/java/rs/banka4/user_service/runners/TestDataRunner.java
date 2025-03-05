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
        Optional<Employee> newEmployeeMarko = employeeRepository.findByUsername(newUsername);

        if (newEmployeeMarko.isEmpty()) {
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

        newEmployeeMarko = employeeRepository.findByUsername(newUsername);

        String newClientEmail = "mkarisik@raf.rs";
        String newClientEmailSecond = "iatanas@raf.rs";

        Optional<Client> newClientMehmedalija = clientRepository.findByEmail(newClientEmail);

        if (newClientMehmedalija.isEmpty()) {
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

            clientRepository.save(newClient);
        }

        newClientMehmedalija = clientRepository.findByEmail(newClientEmail);

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

        String accountNumber = "102-39483947329";
        String accountNumber1 = "102-39483947559";

        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            Account account1 = Account.builder()
                    .id(UUID.randomUUID())
                    .accountNumber(accountNumber)
                    .balance(BigDecimal.valueOf(5000.00))
                    .availableBalance(BigDecimal.valueOf(4500.00))
                    .accountMaintenance(BigDecimal.valueOf(100.00))
                    .createdDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusYears(5))
                    .active(true)
                    .accountType(AccountType.STANDARD)
                    .dailyLimit(BigDecimal.valueOf(1000.00))
                    .monthlyLimit(BigDecimal.valueOf(5000.00))
                    .employee(newEmployeeMarko.get())
                    .client(newClientMehmedalija.get())
                    .company(null)
                    .currency(currencyRepository.findByCode(Currency.Code.EUR))
                    .build();

            accountRepository.save(account1);
        }

        if (!accountRepository.existsByAccountNumber(accountNumber1)) {
            Account account1 = Account.builder()
                    .id(UUID.randomUUID())
                    .accountNumber(accountNumber1)
                    .balance(BigDecimal.valueOf(5000.00))
                    .availableBalance(BigDecimal.valueOf(4500.00))
                    .accountMaintenance(BigDecimal.valueOf(100.00))
                    .createdDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusYears(5))
                    .active(true)
                    .accountType(AccountType.STANDARD)
                    .dailyLimit(BigDecimal.valueOf(1000.00))
                    .monthlyLimit(BigDecimal.valueOf(5000.00))
                    .employee(newEmployeeMarko.get())
                    .client(newClientMehmedalija.get())
                    .company(null)
                    .currency(currencyRepository.findByCode(Currency.Code.RSD))
                    .build();

            accountRepository.save(account1);
        }
    }
}
