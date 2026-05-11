package com.example.tune_share_hub_backend.controller;

import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthCheckController {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 실행 중인지 확인합니다.")
    @GetMapping("/api/health")
    public ApiResponseDto<Map<String, String>> healthCheck() {
        return ApiResponseDto.success(Map.of("status", "UP"), "서버가 정상 동작 중입니다.");
    }
}
