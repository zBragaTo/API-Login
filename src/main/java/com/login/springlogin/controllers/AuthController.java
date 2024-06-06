package com.login.springlogin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.login.springlogin.models.User;
import com.login.springlogin.service.EmailService;
import com.login.springlogin.service.UserService;
import com.login.springlogin.token.JwtTokenUtil;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        User newUser = userService.saveUser(user);
        String token = JwtTokenUtil.generateEmailVerificationToken(newUser.getEmail());
        String verificationLink = "http://localhost:8080/api/users/verify?token=" + token;
        emailService.sendSimpleMessage(newUser.getEmail(), "Verificação de Email", "Clique no link para verificar seu email: " + verificationLink);
        return ResponseEntity.ok("Registro bem-sucedido. Verifique seu email para ativar sua conta.");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
            try {
        boolean emailChanged = userService.updateUser(id, updatedUser);
        if (emailChanged) {
            User user = userService.findById(id); 
            String token = JwtTokenUtil.generateEmailVerificationToken(user.getEmail());
            String verificationLink = "http://localhost:8080/api/users/verify?token=" + token;
            emailService.sendSimpleMessage(user.getEmail(), "Verificação de Email", "Clique no link para verificar seu email: " + verificationLink);
            return ResponseEntity.ok("Usuário atualizado com sucesso. \n Verifique seu novo e-mail para ativar sua conta.");
        } else {
            return ResponseEntity.ok("Usuário atualizado com sucesso.");
        }
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {

        if (JwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expirado.");
        }
        
    String email = JwtTokenUtil.getEmailFromToken(token);
        if (email == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido ou expirado.");
    }
    User user = userService.findByEmail(email);
        if (user == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("E-mail não encontrado.");
    }
        if (user.isEnabled()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("E-mail já verificado.");
    }
        user.setEnabled(true);
        userService.saveUserWithoutCheckEmail(user);
            return ResponseEntity.ok("E-mail verificado com sucesso.");
}
    
}
