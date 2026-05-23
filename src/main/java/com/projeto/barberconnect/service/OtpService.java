package com.projeto.barberconnect.service;

import com.projeto.barberconnect.entity.OtpPurpose;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.entity.UserOtp;
import com.projeto.barberconnect.exception.InvalidOtpException;
import com.projeto.barberconnect.repository.UserOtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class OtpService {
    private final UserOtpRepository  userOtpRepository;
    private final SecureRandom  random = new SecureRandom();

    @Value("${otp.expiration-minutes:10}")
    private long expirationMinutes;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    public OtpService(UserOtpRepository userOtpRepository) {
        this.userOtpRepository = userOtpRepository;
    }

    @Transactional
    public String createOtp(User user, OtpPurpose purpose){
        consumePreviousOtps(user, purpose);

        String code = generateCode();
        String codeHash = hashCode(code);

        UserOtp otp = new UserOtp();
        otp.setUser(user);
        otp.setPurpose(purpose);
        otp.setCodeHash(codeHash);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));

        userOtpRepository.save(otp);

        return code;
    }

    @Transactional
    public void validateOtp(User user, OtpPurpose purpose, String code) {
        UserOtp otp = userOtpRepository
                .findTopByUserAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(user, purpose)
                .orElseThrow(() -> new InvalidOtpException("Invalid code"));

        if (!otp.isActive()) {
            throw new InvalidOtpException("Invalid code");
        }

        if (otp.getAttempts() >= maxAttempts) {
            throw new InvalidOtpException("Invalid code");
        }

        otp.incrementAttempts();

        if (!otp.getCodeHash().equals(hashCode(code))) {
            throw new InvalidOtpException("Invalid code");
        }

        otp.consume();
    }

    private void consumePreviousOtps(User user, OtpPurpose purpose) {
        userOtpRepository.findByUserAndPurposeAndConsumedAtIsNull(user, purpose)
                .forEach(UserOtp::consume);
    }

    private String generateCode() {
        int number = random.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private String hashCode(String code){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(code.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash OTP code", e);
        }
    }
}
