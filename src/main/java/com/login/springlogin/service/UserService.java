package com.login.springlogin.service;

import org.springframework.stereotype.Service;
import com.login.springlogin.repositories.UserRepository;
import com.login.springlogin.models.User;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User saveUser(User user){
        String senhaCriptografada = passwordEncoder.encode(user.getPassword());
        user.setPassword(senhaCriptografada);
        return userRepository.save(user);
    }

    public Optional<User> findEmail(String email){
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

}
