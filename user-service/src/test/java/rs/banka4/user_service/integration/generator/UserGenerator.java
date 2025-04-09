package rs.banka4.user_service.integration.generator;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rs.banka4.rafeisen.common.dto.Gender;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.user.User;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.EmployeeService;

/**
 * Provides helper functions for getting instances of users into the database, whether they be
 * customers or employees. Useful for the "Given"/"Arrange" phase of a test.
 */
@AllArgsConstructor
@Service
public class UserGenerator {
    /** Digest of the word "test". */
    public static final String TEST_PASSWORD_HASH =
        "$2a$10$WWXuuyxJN3HB0Y.rL.ffQuK5EMo29ayyfy8jIZTSjUmU99RdMijdW";

    /**
     * A {@code T -> T} function taking a {@link User} builder and returning it unchanged. Useful
     * for providing customization to tests.
     */
    @FunctionalInterface
    public static interface UserCustomizer<E extends User.UserBuilder<?, ?>> {
        E apply(E foo);
    }

    private EmployeeRepository employeeRepo;

    /**
     * Create and insert into the database an employee customized per {@code customizer}.
     *
     * <p>
     * By default, this method creates a user with the following specification:
     *
     * {@snippet :
     * Employee.builder()
     *     .firstName("John")
     *     .lastName("Doe")
     *     .dateOfBirth(LocalDate.of(1990, 1, 1))
     *     .gender("Male")
     *     .email("john.doe@example.com")
     *     .phone("+381660145278")
     *     .address("123 Main St")
     *     .password(TEST_PASSWORD_HASH)
     *     .username("johndoe")
     *     .position("Developer")
     *     .department("IT")
     *     .active(true)
     *     .enabled(true)
     *     .permissionBits(1L)
     * }
     *
     * <p>
     * Flushes the newly-created user into the database as a side-effect.
     */
    public void createEmployee(UserCustomizer<Employee.EmployeeBuilder<?, ?>> customizer) {
        employeeRepo.save(
            customizer.apply(
                /* Keep in sync with the Javadoc above. */
                Employee.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .email("john.doe@example.com")
                    .phone("+381670123654")
                    .address("123 Main St")
                    .password(TEST_PASSWORD_HASH)
                    .username("johndoe")
                    .position("Developer")
                    .department("IT")
                    .active(true)
                    .enabled(true)
                    .permissionBits(1L)
            )
                .build()
        );
        employeeRepo.flush();
    }

    public void createEmployeeLogin(UserCustomizer<Employee.EmployeeBuilder<?, ?>> customizer) {
        employeeRepo.save(
            customizer.apply(
                /* Keep in sync with the Javadoc above. */
                Employee.builder()
                    .id(UUID.fromString("6ea50113-da6f-4693-b9d3-ac27f807d7f5"))
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .email("john.doe@example.com")
                    .phone("+381670123654")
                    .address("123 Main St")
                    .password(TEST_PASSWORD_HASH)
                    .username("johndoe")
                    .position("Developer")
                    .department("IT")
                    .active(true)
                    .enabled(true)
                    .permissionBits(1L)
            )
                .build()
        );
        employeeRepo.flush();
    }

    private final EmployeeService employeeService;

    /**
     * Create and return the response of a login request for an employee with email {@code email}
     * and password {@code password}.
     *
     * @param email Employee email.
     * @param password Employee password.
     * @throws Throwable If login fails.
     * @returns Tokens produced by logging in.
     */
    public LoginResponseDto doEmployeeLogin(String email, String password) {
        return employeeService.login(new LoginDto(email, password));
    }

    private final ClientService clientService;

    /**
     * Create and return the response of a login request for a client with email {@code email} and
     * password {@code password}.
     *
     * @param email Client email.
     * @param password Client password.
     * @throws Throwable If login fails.
     * @returns Tokens produced by logging in.
     */
    public LoginResponseDto doClientLogin(String email, String password) {
        return clientService.login(new LoginDto(email, password));
    }

    private ClientRepository clientRepo;

    /**
     * Create and insert into the database a client customized per {@code customizer}.
     *
     * <p>
     * By default, this method creates a user with the following specification:
     *
     * {@snippet :
     * Client.builder()
     *     .id(UUID.fromString("6f72db23-afc8-4d71-b392-eb9e626ed9af"))
     *     .firstName("John")
     *     .lastName("Doe")
     *     .dateOfBirth(LocalDate.of(1990, 1, 1))
     *     .gender("Male")
     *     .email("john.doe@example.com")
     *     .phone("+381651403267")
     *     .address("123 Main St")
     *     .password(TEST_PASSWORD_HASH)
     *     .enabled(true)
     *     .permissionBits(1L)
     *     .accounts(new HashSet<>())
     *     .contacts(new HashSet<>())
     * }
     *
     * <p>
     * Flushes the newly-created user into the database as a side-effect.
     */
    public void createClient(UserCustomizer<Client.ClientBuilder<?, ?>> customizer) {
        clientRepo.save(
            customizer.apply(
                /* Keep in sync with the Javadoc above. */
                Client.builder()
                    .id(UUID.fromString("6f72db23-afc8-4d71-b392-eb9e626ed9af"))
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .email("john.doe@example.com")
                    .phone("+381664013258")
                    .address("123 Main St")
                    .password(TEST_PASSWORD_HASH)
                    .enabled(true)
                    .permissionBits(1L)
                    .accounts(new HashSet<>())
            )
                .build()
        );
        clientRepo.flush();
    }
}
