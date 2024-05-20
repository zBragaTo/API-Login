package com.login.springlogin.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.login.springlogin.models.User;

public interface UserRepository extends JpaRepository<User, Long>{
    
    Optional<User> findByEmail(String email);

}
