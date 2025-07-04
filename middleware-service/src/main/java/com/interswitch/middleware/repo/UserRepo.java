package com.interswitch.middleware.repo;

import com.interswitch.middleware.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<User, Long> {
    Optional<User> findByBvn(String bvn);
    Optional<User> findByPhoneNumber(String phoneNumber);
}
