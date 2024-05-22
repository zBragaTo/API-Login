package com.login.springlogin.service;

import org.springframework.stereotype.Service;

import com.login.springlogin.repositories.UserRepository;
import com.login.springlogin.dto.mapper.UserDTOMapper;
import com.login.springlogin.dto.response.UserDTO;
import com.login.springlogin.models.User;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserDTOMapper userDTOMapper;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, UserDTOMapper userDTOMapper){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDTOMapper = userDTOMapper;
    }

    public User saveUser(User user){
        Optional<User> usuarioExistente = userRepository.findByEmail(user.getEmail());
        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }

        String senhaCriptografada = passwordEncoder.encode(user.getPassword());
        user.setPassword(senhaCriptografada);
        return userRepository.save(user);
    }
    

    public Optional<User> findEmail(String email){
        return userRepository.findByEmail(email);
    }

    public List<UserDTO> findAll(){
        return Optional.of(userRepository.findAll())
        .filter(list -> !list.isEmpty())
        .map(list -> list.stream().map(userDTOMapper).toList())
        .orElseThrow(() -> new RuntimeException("Nenhum usuário encontrado!."));
    }

    public User updateUser(Long id, User updatedUser){
        Optional<User> existingUserOptional = userRepository.findById(id);
        User existingUser = existingUserOptional.get();
        existingUser.setUser(updatedUser.getUser());
        existingUser.setEmail(updatedUser.getEmail());
        if(updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()){
            String senhaCriptografada = passwordEncoder.encode(updatedUser.getPassword());
            existingUser.setPassword(senhaCriptografada);
        }
        return userRepository.save(existingUser);
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
