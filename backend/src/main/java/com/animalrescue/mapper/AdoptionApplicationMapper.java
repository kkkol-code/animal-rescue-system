package com.animalrescue.mapper;

import com.animalrescue.entity.AdoptionApplication;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdoptionApplicationMapper {

    @Insert("INSERT INTO adoption_applications (adopter_id, animal_id, apply_date, status, remark) " +
            "VALUES (#{adopterId}, #{animalId}, #{applyDate}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "applicationId")
    int insert(AdoptionApplication app);

    @Select("SELECT app.application_id, app.apply_date, app.status, app.remark, " +
            "ad.real_name AS adopter_name, ad.phone, ad.id_card, ad.house_condition, " +
            "a.name AS animal_name, a.breed, a.species, a.gender " +
            "FROM adoption_applications app " +
            "JOIN adopters ad ON app.adopter_id = ad.adopter_id " +
            "JOIN animals a ON app.animal_id = a.animal_id " +
            "WHERE app.status = 'pending' " +
            "ORDER BY app.apply_date DESC")
    List<Map<String, Object>> findPendingApplications();

    @Update("UPDATE adoption_applications SET status = #{status} WHERE application_id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT * FROM adoption_applications WHERE application_id = #{id}")
    AdoptionApplication findById(@Param("id") Long id);
}
