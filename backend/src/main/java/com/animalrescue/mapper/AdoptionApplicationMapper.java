package com.animalrescue.mapper;

import com.animalrescue.entity.AdoptionApplication;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdoptionApplicationMapper {

    @Insert("INSERT INTO adoption_applications (adopter_id, animal_id, apply_date, status, remark) " +
            "VALUES (#{adopterId}, #{animalId}, #{applyDate}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "applicationId")
    int insert(AdoptionApplication app);
}
