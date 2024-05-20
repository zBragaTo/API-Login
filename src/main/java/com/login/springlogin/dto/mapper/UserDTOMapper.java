package com.login.springlogin.dto.mapper;

import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.login.springlogin.dto.response.UserDTO;
import com.login.springlogin.models.User;

@Service
public class UserDTOMapper implements Function<User, UserDTO>{

    @Override
    public UserDTO apply(User user){
        return new UserDTO(
            user.getUser(),
            user.getEmail()
        );
    }
    
}
