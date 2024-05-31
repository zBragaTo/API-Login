package com.login.springlogin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.login.springlogin.service.EmailService;

@RestController
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {
        String subject = "Teste Email";
        String text = "MENSAGEM TESTE";
        emailService.sendSimpleMessage(to, subject, text);
        return ResponseEntity.ok("Email enviado com sucesso para: " + to);
    }
}
