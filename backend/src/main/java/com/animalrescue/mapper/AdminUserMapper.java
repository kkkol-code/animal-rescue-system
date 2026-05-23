package com.animalrescue.mapper;

import com.animalrescue.entity.AdminUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminUserMapper {

    @Select("SELECT * FROM admin_users WHERE username = #{username}")
    AdminUser findByUsername(@Param("username") String username);
}
