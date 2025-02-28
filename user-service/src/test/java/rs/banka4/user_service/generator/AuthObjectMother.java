package rs.banka4.user_service.generator;

import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.VerificationCode;

import java.time.LocalDateTime;

public class AuthObjectMother {

    public static EmployeeVerificationRequestDto generateEmployeeVerificationRequestDto(String password, String code) {
        return new EmployeeVerificationRequestDto(password, code);
    }

    public static VerificationCode generateVerificationCode(String email, String code, boolean used, LocalDateTime expirationDate) {
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setUsed(used);
        verificationCode.setExpirationDate(expirationDate);
        return verificationCode;
    }

    public static Employee generateEmployee(String firstName, String lastName, String email, String position) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPosition(position);
        return employee;
    }

}
