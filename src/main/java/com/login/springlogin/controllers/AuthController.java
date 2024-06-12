package com.login.springlogin.controllers;

import java.util.Map;
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
import com.login.springlogin.service.UserService;
import com.login.springlogin.token.JwtTokenUtil;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            userService.saveUser(user);
            return ResponseEntity.ok("Registro bem-sucedido. Verifique seu email para ativar sua conta.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
            try {
        boolean emailChanged = userService.updateUser(id, updatedUser);
        if (emailChanged) {
            return ResponseEntity.ok("Usuário atualizado com sucesso. \n Verifique seu novo e-mail para ativar sua conta.");
        } else {
            return ResponseEntity.ok("Usuário atualizado com sucesso.");
        }
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email é obrigatório.");
        }
        
        try {
            userService.sendPasswordResetEmail(email);
            return ResponseEntity.ok("Email de recuperação enviado com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nova senha é obrigatória.");
        }
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Senha alterada com sucesso.");
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
