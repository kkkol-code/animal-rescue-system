package com.animalrescue.mapper;

import com.animalrescue.entity.Adopter;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdopterMapper {

    /** 按身份证号查询领养人 */
    @Select("SELECT * FROM adopters WHERE id_card = #{idCard}")
    Adopter findByIdCard(@Param("idCard") String idCard);

    /** 新增领养人，返回自增主键 */
    @Insert("INSERT INTO adopters (real_name, phone, id_card, house_condition) " +
            "VALUES (#{realName}, #{phone}, #{idCard}, #{houseCondition})")
    @Options(useGeneratedKeys = true, keyProperty = "adopterId")
    int insert(Adopter adopter);
}
