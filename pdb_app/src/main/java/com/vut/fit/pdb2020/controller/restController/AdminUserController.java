package com.vut.fit.pdb2020.controller.restController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vut.fit.pdb2020.converter.UserDtoConverter;
import com.vut.fit.pdb2020.dto.UserDto;
import com.vut.fit.pdb2020.entity.User;
import com.vut.fit.pdb2020.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDtoConverter userDtoConverter;

    @Autowired
    private ObjectMapper jsonObjectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/api/admin/users")
    public List<UserDto> getAllUsers() {

        List<User> allUsers = userService.findAll();
        List<UserDto> allUsersDto = new ArrayList<>();

        for (User user: allUsers) {
            allUsersDto.add(userDtoConverter.convertToUserDto(user));
        }

        return allUsersDto;
    }

    @GetMapping("/api/admin/users/{userId}")
    public UserDto getUser(@PathVariable("userId") Long userId) {

        User user = userService.findById(userId);

        return userDtoConverter.convertToUserDto(user);
    }

    @PostMapping("/api/admin/users/create")
    public HashMap<String, String> createUser(@RequestBody String userJSON) throws JsonProcessingException {

        HashMap<String, String> returnCode = new HashMap<>();

        UserDto userDto = jsonObjectMapper.readValue(userJSON, UserDto.class);

        if(userService.findByEmail(userDto.getEmail()) != null) {
            returnCode.put("409", "There is already a user registered with the email provided");

            return returnCode;
        }

        User user = userDtoConverter.convertToUser(userDto);

        userService.save(user);

        returnCode.put("201", "User registered");

        return returnCode;
    }

    @PostMapping("/api/admin/users/update")
    public HashMap<String, String> updateUser(@RequestBody String userJSON) throws JsonProcessingException {

        HashMap<String, String> returnCode = new HashMap<>();

        UserDto userDto = jsonObjectMapper.readValue(userJSON, UserDto.class);

        User user = userService.findById(userDto.getId());

        if(user == null) {
            returnCode.put("409", "There is no user with this id");

            return returnCode;
        }

        /* Check unique email */
        if(!user.getEmail().equals(userDto.getEmail())) {

            if(userService.findByEmail(userDto.getEmail()) != null) {
                returnCode.put("409", "There is already a user with the email provided");

                return returnCode;
            }
        }


        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        user.setSurName(userDto.getSurname());
        user.setAddress(userDto.getAddress());
        user.setCity(userDto.getCity());

        userService.save(user);

        returnCode.put("201", "User updated");

        return returnCode;
    }
}
