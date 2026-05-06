# GPT 학습용 코드 묶음

## GPT에게 요청할 말

아래 코드는 Spring Boot + MyBatis 기반 백엔드 프로젝트에서 오늘 작성한 플레이리스트 관련 API 코드입니다.

내가 학습할 수 있도록 다음 관점에서 설명해 주세요.

1. 전체 코드 흐름
2. Controller, Service, Mapper, Mapper XML의 역할
3. DTO가 왜 필요한지
4. `@PathVariable`, `@RequestBody`, `@PutMapping`, `@PatchMapping`의 역할
5. `userId = 1L` 더미값을 왜 쓰고 있는지
6. `update` 결과가 0일 때 왜 실패 처리하는지
7. 1-1 플레이리스트 수정 API와 1-3 공개/비공개 설정 API의 차이
8. 이상하거나 불필요한 코드가 있는지

현재 JWT 인증 기능은 아직 연결하지 않았고, 팀 지침에 따라 테스트용 사용자 ID `1L`을 사용합니다.

---

## 구현한 API

### 1-1. 플레이리스트 수정

```http
PUT /api/playlists/{id}
Content-Type: application/json
```

```json
{
  "title": "수정된 플레이리스트 제목",
  "description": "수정된 플레이리스트 설명",
  "coverImageUrl": "https://example.com/cover.jpg",
  "publicYn": "Y"
}
```

### 1-3. 플레이리스트 공개/비공개 설정

```http
PATCH /api/playlists/{id}/visibility
Content-Type: application/json
```

```json
{
  "publicYn": "N"
}
```

---

## 1. PlaylistController.java

경로:

```text
src/main/java/com/example/tune_share_hub_backend/controller/playlist/PlaylistController.java
```

```java
package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistVisibilityUpdateRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistUpdateRequestDto;
import com.example.tune_share_hub_backend.service.playlist.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {
    // 플레이리스트 관련 비즈니스 로직을 처리하는 서비스이다.
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    // 1-1. 플레이리스트 제목, 설명, 커버 이미지, 공개 여부를 한 번에 수정한다.
    @Operation(summary = "플레이리스트 수정", description = "로그인한 사용자가 본인 소유 플레이리스트를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylist(
            // URL의 {id} 값을 playlistId 매개변수로 받는다.
            @PathVariable("id") Long playlistId,
            // 요청 JSON body를 PlaylistUpdateRequestDto 객체로 변환해서 받는다.
            @RequestBody PlaylistUpdateRequestDto request
    ) {
        // 현재는 인증 기능이 없으므로 USER_ID가 1인 사용자로 테스트한다.
        // TODO: JWT 인증 기능이 완성되면 토큰에서 로그인 사용자 ID를 꺼내도록 변경한다.
        Long userId = 1L;
        playlistService.updatePlaylist(playlistId, userId, request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트가 수정되었습니다."));
    }

    // 1-3. 플레이리스트의 공개 여부만 별도로 변경한다.
    @Operation(summary = "플레이리스트 공개 여부 설정", description = "로그인한 사용자가 본인 소유 플레이리스트의 공개 여부를 변경합니다.")
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylistVisibility(
            // URL의 {id} 값을 playlistId 매개변수로 받는다.
            @PathVariable("id") Long playlistId,
            // 요청 JSON body를 PlaylistVisibilityUpdateRequestDto 객체로 변환해서 받는다.
            @RequestBody PlaylistVisibilityUpdateRequestDto request
    ) {
        // 현재는 인증 기능이 없으므로 USER_ID가 1인 사용자로 테스트한다.
        // TODO: JWT 인증 기능이 완성되면 토큰에서 로그인 사용자 ID를 꺼내도록 변경한다.
        Long userId = 1L;
        playlistService.updatePlaylistVisibility(playlistId, userId, request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트 공개 여부가 변경되었습니다."));
    }
}
```

---

## 2. PlaylistService.java

경로:

```text
src/main/java/com/example/tune_share_hub_backend/service/playlist/PlaylistService.java
```

```java
package com.example.tune_share_hub_backend.service.playlist;

import com.example.tune_share_hub_backend.dto.playlist.PlaylistVisibilityUpdateRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistUpdateRequestDto;
import com.example.tune_share_hub_backend.exception.ApiException;
import com.example.tune_share_hub_backend.mapper.playlist.PlaylistMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistService {
    // MyBatis XML에 정의된 SQL을 호출하는 Mapper이다.
    private final PlaylistMapper playlistMapper;

    public PlaylistService(PlaylistMapper playlistMapper) {
        this.playlistMapper = playlistMapper;
    }

    @Transactional
    public void updatePlaylist(Long playlistId, Long userId, PlaylistUpdateRequestDto request) {
        // 요청값 검증을 먼저 끝낸 뒤 DB update를 시도한다.
        validatePlaylistId(playlistId);
        validateRequest(request);

        // UPDATE 조건에 playlistId와 userId가 함께 들어가므로, 본인 소유 플레이리스트만 수정된다.
        int updatedCount = playlistMapper.updatePlaylist(playlistId, userId, request);
        if (updatedCount == 0) {
            // 수정된 행이 없으면 존재하지 않거나, 삭제되었거나, 다른 사용자의 플레이리스트로 판단한다.
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "PLAYLIST_UPDATE_FORBIDDEN",
                    "플레이리스트를 수정할 수 없습니다."
            );
        }
    }

    @Transactional
    public void updatePlaylistVisibility(Long playlistId, Long userId, PlaylistVisibilityUpdateRequestDto request) {
        // 공개 여부 변경 API는 publicYn만 검증하면 된다.
        validatePlaylistId(playlistId);
        validateVisibilityRequest(request);

        // 공개 여부만 바꾸는 전용 API이지만, 본인 소유 검증 조건은 플레이리스트 수정과 동일하다.
        int updatedCount = playlistMapper.updatePlaylistVisibility(playlistId, userId, request.getPublicYn());
        if (updatedCount == 0) {
            // 수정된 행이 없으면 존재하지 않거나, 삭제되었거나, 다른 사용자의 플레이리스트로 판단한다.
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "PLAYLIST_VISIBILITY_UPDATE_FORBIDDEN",
                    "플레이리스트 공개 여부를 변경할 수 없습니다."
            );
        }
    }

    private void validatePlaylistId(Long playlistId) {
        // URL 경로로 받은 playlistId가 잘못된 값이면 DB에 접근하지 않는다.
        if (playlistId == null || playlistId <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PLAYLIST_ID", "유효하지 않은 플레이리스트 ID입니다.");
        }
    }

    private void validateRequest(PlaylistUpdateRequestDto request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 본문이 필요합니다.");
        }

        // PLAYLISTS.TITLE은 NOT NULL이므로 빈 제목은 미리 차단한다.
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PLAYLIST_TITLE", "플레이리스트 제목은 필수입니다.");
        }

        validatePublicYn(request.getPublicYn());
    }

    private void validateVisibilityRequest(PlaylistVisibilityUpdateRequestDto request) {
        // 공개 여부만 변경하더라도 요청 본문 자체는 반드시 필요하다.
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 본문이 필요합니다.");
        }

        validatePublicYn(request.getPublicYn());
    }

    private void validatePublicYn(String publicYn) {
        // DB 제약 조건과 동일하게 PUBLIC_YN은 Y 또는 N만 허용한다.
        if (!"Y".equals(publicYn) && !"N".equals(publicYn)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PUBLIC_YN", "공개 여부는 Y 또는 N만 가능합니다.");
        }
    }
}
```

---

## 3. PlaylistMapper.java

경로:

```text
src/main/java/com/example/tune_share_hub_backend/mapper/playlist/PlaylistMapper.java
```

```java
package com.example.tune_share_hub_backend.mapper.playlist;

import com.example.tune_share_hub_backend.dto.playlist.PlaylistUpdateRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlaylistMapper {
    // 1-1. 플레이리스트 기본 정보를 수정한다. 성공 시 1, 조건에 맞는 플레이리스트가 없으면 0을 반환한다.
    int updatePlaylist(@Param("playlistId") Long playlistId,
                       @Param("userId") Long userId,
                       @Param("request") PlaylistUpdateRequestDto request);

    // 1-3. 공개 여부만 수정한다. 성공 시 1, 조건에 맞는 플레이리스트가 없으면 0을 반환한다.
    int updatePlaylistVisibility(@Param("playlistId") Long playlistId,
                                 @Param("userId") Long userId,
                                 @Param("publicYn") String publicYn);
}
```

---

## 4. PlaylistMapper.xml

경로:

```text
src/main/resources/mapper/playlist/PlaylistMapper.xml
```

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.tune_share_hub_backend.mapper.playlist.PlaylistMapper">

    <!-- 1-1. 플레이리스트 기본 정보 수정 -->
    <update id="updatePlaylist">
        UPDATE PLAYLISTS
        SET TITLE = #{request.title},
            DESCRIPTION = #{request.description},
            COVER_IMAGE_URL = #{request.coverImageUrl},
            PUBLIC_YN = #{request.publicYn},
            UPDATED_AT = SYSTIMESTAMP
        WHERE PLAYLIST_ID = #{playlistId}
          AND USER_ID = #{userId}
          AND DELETED_AT IS NULL
    </update>

    <!-- 1-3. 플레이리스트 공개 여부만 수정 -->
    <update id="updatePlaylistVisibility">
        UPDATE PLAYLISTS
        SET PUBLIC_YN = #{publicYn},
            UPDATED_AT = SYSTIMESTAMP
        WHERE PLAYLIST_ID = #{playlistId}
          AND USER_ID = #{userId}
          AND DELETED_AT IS NULL
    </update>
</mapper>
```

---

## 5. PlaylistUpdateRequestDto.java

경로:

```text
src/main/java/com/example/tune_share_hub_backend/dto/playlist/PlaylistUpdateRequestDto.java
```

```java
package com.example.tune_share_hub_backend.dto.playlist;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistUpdateRequestDto {
    private String title;
    private String description;
    private String coverImageUrl;
    private String publicYn;
}
```

---

## 6. PlaylistVisibilityUpdateRequestDto.java

경로:

```text
src/main/java/com/example/tune_share_hub_backend/dto/playlist/PlaylistVisibilityUpdateRequestDto.java
```

```java
package com.example.tune_share_hub_backend.dto.playlist;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistVisibilityUpdateRequestDto {
    // 공개 여부 값이다. Y이면 공개, N이면 비공개이다.
    private String publicYn;
}
```

---

## 7. ApiResponseDto.java

경로:

```text
src/main/java/com/example/tune_share_hub_backend/dto/common/ApiResponseDto.java
```

```java
package com.example.tune_share_hub_backend.dto.common;

import lombok.Getter;

@Getter
public class ApiResponseDto<T> {
    private final boolean success;
    private final T data;
    private final String message;

    private ApiResponseDto(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return new ApiResponseDto<>(true, data, message);
    }
}
```

---

## 8. ApiErrorResponseDto.java

경로:

```text
src/main/java/com/example/tune_share_hub_backend/dto/common/ApiErrorResponseDto.java
```

```java
package com.example.tune_share_hub_backend.dto.common;

import lombok.Getter;

@Getter
public class ApiErrorResponseDto {
    private final boolean success = false;
    private final String errorCode;
    private final String message;

    public ApiErrorResponseDto(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
```

---

## 9. ApiException.java

경로:

```text
src/main/java/com/example/tune_share_hub_backend/exception/ApiException.java
```

```java
package com.example.tune_share_hub_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
```

---

## 10. GlobalExceptionHandler.java

경로:

```text
src/main/java/com/example/tune_share_hub_backend/exception/GlobalExceptionHandler.java
```

```java
package com.example.tune_share_hub_backend.exception;

import com.example.tune_share_hub_backend.dto.common.ApiErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스나 인터셉터에서 발생시킨 ApiException을 공통 실패 응답 형식으로 변환한다.
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponseDto> handleApiException(ApiException exception) {
        ApiErrorResponseDto response = new ApiErrorResponseDto(exception.getErrorCode(), exception.getMessage());
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    // 예상하지 못한 오류도 JSON 형식으로 응답하기 위한 마지막 안전망이다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(Exception exception) {
        ApiErrorResponseDto response = new ApiErrorResponseDto("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

---

## 11. application.yml에서 추가한 MyBatis 설정

경로:

```text
src/main/resources/application.yml
```

```yaml
mybatis:
  mapper-locations: classpath:mapper/**/*.xml
```

---

## 테스트 결과

실행 명령:

```bash
.\gradlew.bat test
```

결과:

```text
BUILD SUCCESSFUL
```

