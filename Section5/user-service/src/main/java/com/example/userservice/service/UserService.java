package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUserByUserId(String userId);

    // 타입은 가공해서 UserDto로 써도되고 데이터베이스 테이블에 있는 것을 그대로 쓰는 UserEntity도 가능
    Iterable<UserEntity> getUserByAll();
}
