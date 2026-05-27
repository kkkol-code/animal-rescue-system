package com.animalrescue.mapper;

import com.animalrescue.entity.MedicalRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MedicalRecordMapper {

    @Select("SELECT * FROM medical_records WHERE animal_id = #{animalId} ORDER BY check_date DESC")
    List<MedicalRecord> findByAnimalId(@Param("animalId") Long animalId);

    @Select("SELECT * FROM medical_records WHERE record_id = #{id}")
    MedicalRecord findById(@Param("id") Long id);

    @Insert("INSERT INTO medical_records (animal_id, check_date, type, vet_name, weight, temperature, diagnosis, treatment) " +
            "VALUES (#{animalId}, #{checkDate}, #{type}, #{vetName}, #{weight}, #{temperature}, #{diagnosis}, #{treatment})")
    @Options(useGeneratedKeys = true, keyProperty = "recordId")
    int insert(MedicalRecord record);
}
