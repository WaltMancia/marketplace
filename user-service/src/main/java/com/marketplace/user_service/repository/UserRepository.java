package com.marketplace.user_service.repository;

import com.marketplace.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// JpaRepository<User, Long>:
//   User  → la entidad que maneja
//   Long  → el tipo del ID
// Spring genera automáticamente: findAll(), findById(), save(), delete(), etc.
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring lee el nombre del método y genera el SQL automáticamente:
    // "findBy" + "Email" → SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // "existsBy" + "Email" → SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);
}