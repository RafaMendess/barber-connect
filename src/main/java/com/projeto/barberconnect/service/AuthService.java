package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.auth.*;
import com.projeto.barberconnect.entity.OtpPurpose;
import com.projeto.barberconnect.entity.Role;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.EmailAlreadyExistsException;
import com.projeto.barberconnect.exception.EmailNotVerifiedException;
import com.projeto.barberconnect.exception.InvalidCredentialsException;
import com.projeto.barberconnect.exception.InvalidOtpException;
import com.projeto.barberconnect.repository.RoleRepository;
import com.projeto.barberconnect.repository.UserRepository;
import com.projeto.barberconnect.security.jwt.JwtService;
import com.projeto.barberconnect.util.StringNormalizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;
    private final EmailService emailService;



    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RoleRepository roleRepository, RefreshTokenService refreshTokenService, OtpService otpService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.refreshTokenService = refreshTokenService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @Transactional
    public void register(RegisterRequestDto dto) {
        String email = StringNormalizer.normalizeEmail(dto.email());
        boolean userExists = userRepository.existsByEmail(email);

        if (userExists) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();

        user.setEmail(email);
        user.setName(StringNormalizer.trim(dto.name()));

        String encryptedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(encryptedPassword);

        Role clientRole =
                roleRepository
                        .findByName("ROLE_CLIENT")
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Role not found"
                                )
                        );

        user.setRoles(Set.of(clientRole));

        userRepository.save(user);

        sendVerificationCode(user);
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto dto) {
        String email = StringNormalizer.normalizeEmail(dto.email());
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new InvalidCredentialsException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(dto.password(), user.getPassword());

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if(!user.isEmailVerified()){
            throw new EmailNotVerifiedException("Email not verified");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);


        return new AuthResponseDto(accessToken,
                refreshToken,
                "Bearer",
                jwtService.getExpirationInSeconds()
        );
    }

    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto dto){
        User user = refreshTokenService.validateAndRotate(dto.refreshToken());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getExpirationInSeconds()
        );
    }
    @Transactional
    public void verifyEmail(VerifyEmailRequestDto dto){
        String email = StringNormalizer.normalizeEmail(dto.email());

        User user = userRepository.findByEmail(email).
                orElseThrow(()-> new InvalidOtpException("Invalid Code"));

        otpService.validateOtp(user,OtpPurpose.EMAIL_VERIFICATION, dto.code());

        user.setEmailVerified(true);
    }

    @Transactional
    public void resendVerificationCode(ResendVerificationCodeRequestDto dto) {
        String email = StringNormalizer.normalizeEmail(dto.email());

        userRepository.findByEmail(email)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerificationCode);
    }

    @Transactional
    public void sendVerificationCode(User user) {
        String code = otpService.createOtp(user, OtpPurpose.EMAIL_VERIFICATION);
        emailService.sendEmailVerificationCode(user.getEmail(), code);
    }

    @Transactional
    public void logout(LogoutRequestDto dto){
        refreshTokenService.revoke(dto.refreshToken());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto dto){
        String email = StringNormalizer.normalizeEmail(dto.email());

        userRepository.findByEmail(email).ifPresent(user -> {
            String code = otpService.createOtp(user,OtpPurpose.PASSWORD_RESET);
            emailService.sendPasswordResetCode(email,code);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto dto){
        String email = StringNormalizer.normalizeEmail(dto.email());

        User user = userRepository.findByEmail(email).
                orElseThrow(()-> new InvalidOtpException("Invalid otp"));

        otpService.validateOtp(user,OtpPurpose.PASSWORD_RESET, dto.code());

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        refreshTokenService.revokeAllFromUser(user);
        userRepository.save(user);
    }

}
