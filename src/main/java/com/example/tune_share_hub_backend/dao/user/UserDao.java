package com.example.tune_share_hub_backend.dao.user;

import com.example.tune_share_hub_backend.entity.user.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {
    public int insert(User user);
//    public int update(User user);
//    public int delete(Long id);
    public User getUserByEmail(String email);
    public User getUserById(Long id);
}
