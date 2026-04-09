package com.marketplace.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// @Entity le dice a JPA que esta clase es una tabla en la BD
@Entity
// @Table define el nombre de la tabla y sus constraints
@Table(
        name = "users",
        uniqueConstraints = {
                // Constraint de unicidad — no pueden existir dos usuarios con el mismo email
                @UniqueConstraint(columnNames = "email", name = "uk_users_email")
        }
)
// Lombok annotations — generan automáticamente el boilerplate
@Data               // getters, setters, equals, hashCode, toString
@Builder            // patrón Builder: User.builder().name("Juan").build()
@NoArgsConstructor  // constructor vacío requerido por JPA
@AllArgsConstructor // constructor con todos los campos
// UserDetails es la interfaz de Spring Security para usuarios
// Al implementarla, Spring Security sabe cómo usar nuestra entidad
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password; // siempre almacenamos el hash, nunca texto plano

    @Enumerated(EnumType.STRING) // guarda "CUSTOMER" o "SELLER" en vez de 0 o 1
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // @PrePersist se ejecuta automáticamente ANTES de insertar en BD
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // @PreUpdate se ejecuta automáticamente ANTES de actualizar en BD
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Métodos de UserDetails ───────────────────────────────
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // GrantedAuthority representa un permiso o rol
        // Spring Security espera "ROLE_" como prefijo
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // Spring Security usa email como "username"
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }
}