package com.app.AutomationSales.appUser.service;

import com.app.AutomationSales.appUser.UserDto.UserRequestDto;
import com.app.AutomationSales.appUser.UserDto.UserResponseDto;
import com.app.AutomationSales.appUser.UserRepository.UserRepository;
import com.app.AutomationSales.appUser.model.Role;
import com.app.AutomationSales.appUser.model.User;
import com.app.AutomationSales.exceptionHandling.BadRequestException;
import com.app.AutomationSales.exceptionHandling.ResourceNotFoundExceptionHandler;
import com.app.AutomationSales.salesperson.model.SalesPerson;
import com.app.AutomationSales.salesperson.repository.SalesPersonRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           SalesPersonRepository salesPersonRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.salesPersonRepository = salesPersonRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDto createUser(UserRequestDto request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("A user with email " + email + " already exists");
        }

        SalesPerson salesPerson = null;
        if (request.role() == Role.SALESPERSON) {
            if (request.salesPersonId() == null) {
                throw new BadRequestException("salesPersonId is required for a SALESPERSON user");
            }
            salesPerson = salesPersonRepository.findById(request.salesPersonId())
                    .orElseThrow(() -> new ResourceNotFoundExceptionHandler(
                            "SalesPerson not found with id " + request.salesPersonId()));
            if (userRepository.existsBySalesPersonId(salesPerson.getId())) {
                throw new BadRequestException("SalesPerson " + salesPerson.getId() + " already has a login");
            }
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .salesPerson(salesPerson)
                .enabled(true)
                .build();

        return UserResponseDto.from(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllUsers() {
        return userRepository.findAll().stream().map(UserResponseDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findUserById(Long id) {
        return UserResponseDto.from(getUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserResponseDto::from)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler("User not found with email " + email));
    }

    @Override
    public UserResponseDto setEnabled(Long id, boolean enabled) {
        User user = getUser(id);
        user.setEnabled(enabled);
        return UserResponseDto.from(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionHandler("User not found with id " + id));
    }


}
