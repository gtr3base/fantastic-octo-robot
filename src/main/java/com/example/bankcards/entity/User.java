package com.example.bankcards.entity;

import com.example.bankcards.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Entity
@Table(name="users")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    @NotBlank(message = "Name is required")
    private String name;

    @Column(name = "surname", nullable = false, updatable = true)
    @NotBlank(message = "Surname is required")
    private String surname;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 255, message = "Password minimum length is 6 and max 255")
    private String password;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "role", nullable = false)
    @NotNull(message = "Role is required")
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Column(name = "created_at")
    @PastOrPresent(message = "Creation date cannot be in the future")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> cards;
}
