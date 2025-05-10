package com.datpvc.identity_service.controller;

import com.datpvc.identity_service.dto.response.UserResponse;
import com.datpvc.identity_service.dto.request.UserCreationRequest;
import com.datpvc.identity_service.dto.request.UserUpdateRequest;
import com.datpvc.identity_service.dto.response.ApiResponse;
import com.datpvc.identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .message("User created successfully")
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .message("All users successfully")
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUserById(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .message("User successfully")
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .message("My info successfully")
                .build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .message("User updated successfully")
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ApiResponse.builder()
                .message("Successfully deleted user")
                .build();
    }
}