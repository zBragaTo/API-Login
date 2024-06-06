package com.login.springlogin.service;

import org.springframework.stereotype.Service;

import com.login.springlogin.repositories.UserRepository;
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

    public User saveUser(User user){
        Optional<User> usuarioExistente = userRepository.findByEmail(user.getEmail());
        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }    
        if (user.getPassword() != null) {
            String senhaCriptografada = passwordEncoder.encode(user.getPassword());
            user.setPassword(senhaCriptografada);
        }
        return userRepository.save(user);
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
        return userRepository.findByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<UserDTO> findAll(){
        return Optional.of(userRepository.findAll())
        .filter(list -> !list.isEmpty())
        .map(list -> list.stream().map(userDTOMapper).toList())
        .orElseThrow(() -> new RuntimeException("Nenhum usuário encontrado!."));
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
        return emailChanged;
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
