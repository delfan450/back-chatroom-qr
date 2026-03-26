package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Integer> {
    Optional<VerificationCode> findByEmailAndCodeAndUsedFalse(String email, String code);

    @Query("SELECT v FROM VerificationCode v WHERE v.email = :email AND v.used = false ORDER BY v.createdAt DESC")
    List<VerificationCode> findUnusedByEmail(@Param("email") String email);
}
