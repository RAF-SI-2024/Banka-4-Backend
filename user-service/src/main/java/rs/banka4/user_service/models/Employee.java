package rs.banka4.user_service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;

@Entity(name = "employees")
@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Employee extends User {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private boolean active;
}