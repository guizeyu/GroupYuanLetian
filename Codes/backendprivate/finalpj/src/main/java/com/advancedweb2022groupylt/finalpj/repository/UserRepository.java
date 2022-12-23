package com.advancedweb2022groupylt.finalpj.repository;

import com.advancedweb2022groupylt.finalpj.bean.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User,Long>
{
    User findUserByUsername(String username);
}
