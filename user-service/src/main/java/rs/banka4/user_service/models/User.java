package rs.banka4.user_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Stream;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    @Column(nullable = false)
    public LocalDate dateOfBirth;

    @Column(nullable = false)
    public String gender;

    @Email
    @Column(nullable = false, unique = true)
    public String email;

    @Column(nullable = false)
    public String phone;

    @Column(nullable = false)
    public String address;

    @Column(nullable = false)
    public String password;

    @Column(nullable = false)
    public boolean enabled;

    @Column(nullable = false)
    public long permissionBits;

    public EnumSet<Privilege> getPrivileges() {
        return EnumSet.copyOf(
                Stream.of(Privilege.values())
                        .filter(p -> (permissionBits & p.bit()) != 0)
                        .toList()
        );
    }

    public void setPrivileges(Collection<Privilege> privileges) {
        permissionBits = privileges.stream()
                .map(Privilege::bit)
                .reduce(0L, (x, y) -> x | y);
    }
}