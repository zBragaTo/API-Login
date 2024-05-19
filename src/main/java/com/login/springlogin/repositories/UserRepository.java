package com.login.springlogin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.login.springlogin.models.User;

public interface UserRepository extends JpaRepository<User, Long>{
    
    User findByEmail(String email);
}
