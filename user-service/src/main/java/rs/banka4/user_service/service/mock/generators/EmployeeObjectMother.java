package rs.banka4.user_service.service.mock.generators;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import rs.banka4.rafeisen.common.dto.EmployeeResponseDto;
import rs.banka4.rafeisen.common.dto.Gender;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;

public class EmployeeObjectMother {

    public static CreateEmployeeDto generateBasicCreateEmployeeDto() {
        return new CreateEmployeeDto(
            "John",
            "Doe",
            "johndoe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "john.doe@example.com",
            "+381687452196",
            "123 Main St",
            Set.of(),
            "Developer",
            "IT",
            true
        );
    }

    public static LoginDto generateBasicLoginDto() {
        return new LoginDto("user@example.com", "password");
    }

    public static LoginDto generateLoginDtoWithIncorrectPassword() {
        return new LoginDto("user@example.com", "wrong-password");
    }

    public static LoginDto generateLoginDtoWithNonExistentUser() {
        return new LoginDto("nonexistent@example.com", "password");
    }

    public static Employee generateEmployeeWithAllAttributes() {
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setPosition("Developer");
        return employee;
    }

    public static UpdateEmployeeDto generateBasicUpdateEmployeeDto() {
        return new UpdateEmployeeDto(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "john.doe@example.com",
            "johndoe",
            "+381675896321",
            "123 Main St",
            Set.of(),
            "Developer",
            "IT",
            true
        );
    }

    public static UpdateEmployeeDto generateUpdateEmployeeDtoWithDuplicateEmail() {
        return new UpdateEmployeeDto(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "duplicate.email@example.com",
            "johndoe",
            "+381663985214",
            "123 Main St",
            Set.of(),
            "Developer",
            "IT",
            true
        );
    }

    public static UpdateEmployeeDto generateUpdateEmployeeDtoWithDuplicateUsername() {
        return new UpdateEmployeeDto(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "john.doe@example.com",
            "duplicateusername",
            "+381655236987",
            "123 Main St",
            Set.of(),
            "Developer",
            "IT",
            true
        );
    }

    public static UpdateEmployeeDto generateUpdateEmployeeDtoWithNonExistentUser() {
        return new UpdateEmployeeDto(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "nonexistent@example.com",
            "nonexistentuser",
            "+381644789652",
            "123 Main St",
            Set.of(),
            "Developer",
            "IT",
            true
        );
    }

    public static Employee generateBasicEmployee() {
        Employee employee = new Employee();
        employee.setId(UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87"));
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        employee.setGender(Gender.MALE);
        employee.setEmail("john.doe@example.com");
        employee.setUsername("johndoe");
        employee.setPhone("+381635214789");
        employee.setAddress("123 Main St");
        employee.setPrivileges(Set.of(Privilege.ADMIN));
        employee.setPosition("Developer");
        employee.setDepartment("IT");
        employee.setActive(true);
        return employee;
    }

    public static EmployeeDto generateBasicEmployeeDto() {
        return new EmployeeDto(
            UUID.randomUUID(),
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "john.doe@example.com",
            "+381624789635",
            "123 Main St",
            "johndoe",
            "Developer",
            "IT",
            true
        );
    }

    public static EmployeeResponseDto generateBasicEmployeeResponseDto() {
        return new EmployeeResponseDto(
            UUID.randomUUID(),
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "john.doe@example.com",
            "+381617452183",
            "123 Main St",
            "johndoe",
            "Developer",
            "IT",
            EnumSet.noneOf(Privilege.class),
            true
        );
    }

}
