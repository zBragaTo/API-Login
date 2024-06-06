package com.login.springlogin.controllers;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.login.springlogin.dto.response.UserDTO;
import com.login.springlogin.models.User;
import com.login.springlogin.service.EmailService;
import com.login.springlogin.service.UserService;
import com.login.springlogin.token.JwtTokenUtil;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/{email}")
    public ResponseEntity<User> getUser(@PathVariable String email){
        return userService.findEmail(email).
        map(ResponseEntity::ok).
        orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        List<UserDTO> users = userService.findAll();
        return ResponseEntity.ok(users);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
         userService.delete(id);
         return ResponseEntity.ok("Usuário Deletado!");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}

