package com.login.springlogin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.login.springlogin.models.User;
import com.login.springlogin.service.EmailService;
import com.login.springlogin.service.UserService;
import com.login.springlogin.token.JwtTokenUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        User newUser = userService.saveUserWithoutCheckEmail(user);
        String token = JwtTokenUtil.generateEmailVerificationToken(newUser.getEmail());
        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token;
        emailService.sendSimpleMessage(newUser.getEmail(), "Verificação de Email", "Clique no link para verificar seu email: " + verificationLink);
        return ResponseEntity.ok("Registro bem-sucedido. Verifique seu email para ativar sua conta.");
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
