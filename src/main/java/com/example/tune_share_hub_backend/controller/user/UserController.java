package com.example.tune_share_hub_backend.controller.user;

import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
import com.example.tune_share_hub_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다. Access Token이 필요합니다.")
    @AccessTokenCheck
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUserInfo(@LoginUserId Long userId) {
        UserResponseDto userResponseDto = userService.getUserInfo(userId);
        return ResponseEntity.ok(userResponseDto);
    }
}
