package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.OtpPurpose;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.entity.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp,Long> {

    Optional<UserOtp> findTopByUserAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            User user,
            OtpPurpose purpose
    );

    List<UserOtp> findByUserAndPurposeAndConsumedAtIsNull(
            User user,
            OtpPurpose purpose
    );
}
