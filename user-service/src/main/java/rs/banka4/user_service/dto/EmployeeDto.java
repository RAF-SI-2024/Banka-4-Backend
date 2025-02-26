package rs.banka4.user_service.dto;

import java.time.LocalDate;

public record EmployeeDto (
        String id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String email,
        String phone,
        String address,
        String username,
        String position,
        String department
){ }