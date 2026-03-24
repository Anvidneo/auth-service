package com.bank_dugongo.auth_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private Integer id;

    private String username;

    private Integer customerId;

    private Boolean isActive;

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;
}
