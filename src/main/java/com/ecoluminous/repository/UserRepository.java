package com.ecoluminous.repository;

import com.ecoluminous.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    // 💡 이 메서드를 추가해 주세요!
    Optional<User> findByApiKey(String apiKey);
}