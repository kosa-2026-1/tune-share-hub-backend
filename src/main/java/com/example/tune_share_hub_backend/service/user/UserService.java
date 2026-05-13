package com.example.tune_share_hub_backend.service.user;

import com.example.tune_share_hub_backend.convert.UserConvert;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.validate.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserDao userDao;

    @Transactional
    public UserResponseDto getUserInfo(Long userId) {
        UserValidator.validateUserId(userId);
        User user = userDao.getUserById(userId);
        UserValidator.validateUserExists(user);
        return UserConvert.toResponseDto(user);
    }
}
