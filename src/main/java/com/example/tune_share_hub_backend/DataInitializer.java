package com.example.tune_share_hub_backend;

import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.entity.user.UserRoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 더미 사용자 데이터 삽입
        insertDummyUsers();
    }

    private void insertDummyUsers() {
        try {
            // 이미 존재하는지 확인
            if (userDao.getUserByEmail("test@example.com") != null) {
                log.info("더미 데이터가 이미 존재합니다.");
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            // 더미 사용자 1
            User user1 = User.builder()
                    .email("test@example.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .nickname("테스트유저")
                    .role(UserRoleType.USER)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            userDao.insert(user1);
            log.info("더미 사용자 1 생성: {}", user1.getEmail());

            // 더미 사용자 2 (관리자)
            User user2 = User.builder()
                    .email("admin@example.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .nickname("관리자")
                    .role(UserRoleType.ADMIN)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            userDao.insert(user2);
            log.info("더미 사용자 2 생성: {}", user2.getEmail());

        } catch (Exception e) {
            log.error("더미 데이터 삽입 중 오류 발생: ", e);
        }
    }
}
