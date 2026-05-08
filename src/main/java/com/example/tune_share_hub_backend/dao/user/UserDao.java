package com.example.tune_share_hub_backend.dao.user;

import com.example.tune_share_hub_backend.entity.user.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {
    int insert(User user);
//    int update(User user);
//    int delete(Long id);
    User getUserByEmail(String email);
    User getUserById(Long id);
}
