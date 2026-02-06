package fit.hutech.spring.entities;

import fit.hutech.spring.validators.annotations.ValidUsername;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", length = 50, unique = true)
    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters")
    @ValidUsername
    private String username;
    @Column(name = "password", length = 250)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    @Column(name = "email", length = 100, unique = true)
    @NotBlank(message = "Email is required")
    @Size(min = 1, max = 100, message = "Email must be between 1 and 100 characters")
    @Email(message = "Email must be valid format")
    private String email;
    @Column(name = "phone", length = 10, unique = true)
    @Length(min = 10, max = 10, message = "Phone must be 10characters")
    @Pattern(regexp = "^[0-9]*$", message = "Phone must be number")
    private String phone;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private Set<Invoice> invoices = new HashSet<>();
    @Column(name = "provider", length = 50)
    private String provider;
    @Column(name = "shipping_address", length = 255)
    private String shippingAddress;
    @Column(name = "shipping_city", length = 100)
    private String shippingCity;
    @Column(name = "shipping_postal_code", length = 20)
    private String shippingPostalCode;
    @Column(name = "shipping_country", length = 100)
    private String shippingCountry;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Date lockedUntil;

    @Column(name = "created_at")
    @Builder.Default
    private Date createdAt = new Date();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> userRoles = this.getRoles();
        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (lockedUntil == null) {
            return true;
        }
        // If lock time has passed, account is no longer locked
        return new Date().after(lockedUntil);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(),
                user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}