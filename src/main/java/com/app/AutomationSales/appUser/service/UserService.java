package com.app.AutomationSales.appUser.service;

import com.app.AutomationSales.appUser.UserDto.UserRequestDto;
import com.app.AutomationSales.appUser.UserDto.UserResponseDto;
import com.app.AutomationSales.appUser.model.User;

import java.util.List;

public interface UserService {

    UserResponseDto createUser(UserRequestDto request);

    List<UserResponseDto> findAllUsers();

    UserResponseDto findUserById(Long id);

    UserResponseDto findUserByEmail(String email);

    UserResponseDto setEnabled(Long id, boolean enabled);


}
