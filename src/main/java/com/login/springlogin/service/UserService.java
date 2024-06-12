package com.login.springlogin.service;

import org.springframework.stereotype.Service;

import com.login.springlogin.repositories.UserRepository;
import com.login.springlogin.token.JwtTokenUtil;
import com.login.springlogin.dto.mapper.UserDTOMapper;
import com.login.springlogin.dto.response.UserDTO;
import com.login.springlogin.models.User;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Service
public class UserService {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private  UserDTOMapper userDTOMapper;

    @Autowired
    private EmailService emailService;

    public User saveUser(User user){
        Optional<User> usuarioExistente = userRepository.findByEmail(user.getEmail());
        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }    
        if (user.getPassword() != null) {
            String senhaCriptografada = passwordEncoder.encode(user.getPassword());
            user.setPassword(senhaCriptografada);
        }
        User newUser = userRepository.save(user);
        sendVerificationEmail(newUser);
        return newUser;
    }

    public User saveUserWithoutCheckEmail(User user) {
        if (user.getPassword() != null) {
            String senhaCriptografada = passwordEncoder.encode(user.getPassword());
            user.setPassword(senhaCriptografada);
        }
        if(!user.isEnabled()){
            user.setEnabled(false);
        }
        return userRepository.save(user);
    }
    

    public Optional<User> findEmail(String email){
        Optional<User> userOpt = userRepository.findByEmail(email);
        if(userOpt.isPresent()){
            return userOpt;
        } else {
            throw new IllegalArgumentException("Usuário com e-mail " + email + " não encontrado.");
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<UserDTO> findAll(){
        return Optional.of(userRepository.findAll())
        .filter(list -> !list.isEmpty())
        .map(list -> list.stream().map(userDTOMapper).toList())
        .orElseThrow(() -> new IllegalArgumentException("Nenhum usuário encontrado!"));
    }

    public boolean updateUser(Long id, User updatedUser) {
        Optional<User> existingUserOptional = userRepository.findById(id);
        if (!existingUserOptional.isPresent()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        User existingUser = existingUserOptional.get();
        boolean emailChanged = false;

        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(updatedUser.getEmail());
            if (userWithSameEmail.isPresent()) {
                throw new IllegalArgumentException("E-mail já está em uso.");
            }
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setEnabled(false); // Desabilitar até verificar o novo e-mail
            emailChanged = true;
        }

        existingUser.setUser(updatedUser.getUser());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userRepository.save(existingUser);

        if (emailChanged) {
            sendVerificationEmail(existingUser);
        }

        return emailChanged;
    }

    private void sendVerificationEmail(User user) {
        String token = JwtTokenUtil.generateEmailVerificationToken(user.getEmail());
        String verificationLink = "http://localhost:8080/api/users/verify?token=" + token;
        emailService.sendSimpleMessage(user.getEmail(), "Verificação de Email", "Clique no link para verificar seu email: " + verificationLink);
    }

    public void sendPasswordResetEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("E-mail não encontrado.");
        }

        User user = userOpt.get();
        String token = JwtTokenUtil.generateEmailVerificationToken(user.getEmail());
        String resetLink = "http://localhost:8080/api/users/reset-password?token=" + token;
        emailService.sendSimpleMessage(user.getEmail(), "Redefinição de Senha", "Clique no link para redefinir sua senha: " + resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        String email = JwtTokenUtil.getEmailFromToken(token);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Token inválido.");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void delete(Long id){
        Optional<User> usuario = userRepository.findById(id);
        if (usuario.isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Usuário não encontrado.");
        } 
    }

}
