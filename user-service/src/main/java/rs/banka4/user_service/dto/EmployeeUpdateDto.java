package rs.banka4.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import rs.banka4.user_service.models.Privilege;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
//@Getter

@Data
public class EmployeeUpdateDto{
        String firstName;
        String lastName;
        LocalDate dateOfBirth;
        String gender;
        @Email
        String email;
        String password;
        String username;
        String phone;
        String address;
        Set<Privilege> privilege;
        String position;
        String department;
}
