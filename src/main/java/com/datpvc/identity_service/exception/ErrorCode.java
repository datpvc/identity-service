package com.datpvc.identity_service.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception"),
    KEY_INVALID(1001, "Key Invalid"),
    USER_EXISTED(1002, "Username already exists"),
    USERNAME_INVALID(1003, "Username must be at 3 characters"),
    PASSWORD_INVALID(1004, "Password must be at 8 characters"),
    USER_NOT_FOUND(1005, "User not found"),
    LOGIN_FAILED(1006, "Login Failed"),
    UNAUTHORIZED_ACCESS(1007, "Unauthorized Access"),
    ;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    final int code;
    final String message;
}
