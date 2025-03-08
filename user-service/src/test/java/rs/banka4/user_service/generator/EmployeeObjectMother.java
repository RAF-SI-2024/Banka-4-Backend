//package rs.banka4.user_service.generator;
//
//import rs.banka4.user_service.domain.auth.dtos.LoginDto;
//import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
//import rs.banka4.user_service.domain.user.employee.db.Employee;
//import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
//import rs.banka4.user_service.domain.user.Privilege;
//import rs.banka4.user_service.domain.user.User;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Set;
//
//public class EmployeeObjectMother {
//
//    public static CreateEmployeeDto generateBasicCreateEmployeeDto() {
//        return new CreateEmployeeDto(
//                "John", "Doe", "johndoe", LocalDate.of(1990, 1, 1),
//                User.Gender.MALE, "john.doe@example.com", "+1234567890", "123 Main St",
//                Set.of(Privilege.SEARCH), "Developer", "IT", true);
//    }
//
//    public static LoginDto generateBasicLoginDto() {
//        return new LoginDto("user@example.com", "password");
//    }
//
//    public static LoginDto generateLoginDtoWithIncorrectPassword() {
//        return new LoginDto("user@example.com", "wrong-password");
//    }
//
//    public static LoginDto generateLoginDtoWithNonExistentUser() {
//        return new LoginDto("nonexistent@example.com", "password");
//    }
//
//    public static Employee generateEmployeeWithAllAttributes() {
//        Employee employee = new Employee();
//        employee.setFirstName("Johnathan");
//        employee.setLastName("Doe");
//        employee.setEmail("user@example.com");
//        employee.setPosition("Developer");
//        return employee;
//    }
//
//    public static Employee generateEmployeeWithFirstName(String firstName) {
//        Employee employee = new Employee();
//        employee.setFirstName(firstName);
//        return employee;
//    }
//
//    public static Employee generateEmployeeWithLastName(String lastName) {
//        Employee employee = new Employee();
//        employee.setLastName(lastName);
//        return employee;
//    }
//
//    public static Employee generateEmployeeWithEmail(String email) {
//        Employee employee = new Employee();
//        employee.setEmail(email);
//        return employee;
//    }
//
//    public static Employee generateEmployeeWithPosition(String position) {
//        Employee employee = new Employee();
//        employee.setPosition(position);
//        return employee;
//    }
//
//    public static UpdateEmployeeDto generateBasicUpdateEmployeeDto() {
//        return new UpdateEmployeeDto(
//                "John", "Doe", LocalDate.of(1990, 1, 1), "Male",
//                "john.doe@example.com", "johndoe", "+1234567890", "123 Main St",
//                List.of("TRADE_STOCKS", "CONTRACTS"), "Developer", "IT", true);
//    }
//
//    public static UpdateEmployeeDto generateUpdateEmployeeDtoWithDuplicateEmail() {
//        return new UpdateEmployeeDto(
//                "John", "Doe", LocalDate.of(1990, 1, 1), "Male",
//                "duplicate.email@example.com", "johndoe", "+1234567890", "123 Main St",
//                List.of("TRADE_STOCKS", "CONTRACTS"), "Developer", "IT", true);
//    }
//
//    public static UpdateEmployeeDto generateUpdateEmployeeDtoWithDuplicateUsername() {
//        return new UpdateEmployeeDto(
//                "John", "Doe", LocalDate.of(1990, 1, 1), "Male",
//                "john.doe@example.com", "duplicateusername", "+1234567890", "123 Main St",
//                List.of("TRADE_STOCKS", "CONTRACTS"), "Developer", "IT", true);
//    }
//
//    public static UpdateEmployeeDto generateUpdateEmployeeDtoWithNonExistentUser() {
//        return new UpdateEmployeeDto(
//                "John", "Doe", LocalDate.of(1990, 1, 1), "Male",
//                "nonexistent@example.com", "nonexistentuser", "+1234567890", "123 Main St",
//                List.of("TRADE_STOCKS", "CONTRACTS"), "Developer", "IT", true);
//    }
//
//}
