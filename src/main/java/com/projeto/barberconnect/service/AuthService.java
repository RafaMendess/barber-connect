package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.auth.AuthResponseDto;
import com.projeto.barberconnect.dto.auth.LoginRequestDto;
import com.projeto.barberconnect.dto.auth.RegisterRequestDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.EmailAlreadyExistsException;
import com.projeto.barberconnect.exception.InvalidCredentialsException;
import com.projeto.barberconnect.repository.RoleRepository;
import com.projeto.barberconnect.repository.UserRepository;
import com.projeto.barberconnect.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }
    public void register(RegisterRequestDto dto){
        boolean userExists= userRepository.findByEmail(dto.email()).isPresent();

        if(userExists){
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();

        user.setEmail(dto.email());
        user.setName(dto.name());

        String encryptedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(encryptedPassword);

        userRepository.save(user);
    }

    public AuthResponseDto login(LoginRequestDto dto){
        User user = userRepository.findByEmail(dto.email()).orElseThrow(()->
                new InvalidCredentialsException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(dto.password(), user.getPassword());

        if(!passwordMatches){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponseDto(token);
    }
}
