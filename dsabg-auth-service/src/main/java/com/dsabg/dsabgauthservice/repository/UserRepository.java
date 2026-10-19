package com.dsabg.dsabgauthservice.repository;

import com.dsabg.dsabgauthservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users,Long> {
    @Query(value = """
        SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED))
        FROM users
        """,nativeQuery = true)
    Long getMaxIdNumber();
   boolean existsByEmail(String email);
   Optional<Users> findByEmail(String email);
   Optional<Users> findByUsername(String username);
}
