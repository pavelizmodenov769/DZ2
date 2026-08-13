package com.example.userservice.dao;

import com.example.userservice.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {

    User create(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    User update(User user);

    void deleteById(Long id);
}
