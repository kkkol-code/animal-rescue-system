package com.animalrescue.controller;

import com.animalrescue.common.Result;
import com.animalrescue.entity.MedicalRecord;
import com.animalrescue.mapper.MedicalRecordMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin
public class MedicalRecordController {

    private final MedicalRecordMapper medicalRecordMapper;

    public MedicalRecordController(MedicalRecordMapper medicalRecordMapper) {
        this.medicalRecordMapper = medicalRecordMapper;
    }

    @GetMapping
    public Result<List<MedicalRecord>> list(@RequestParam Long animal_id) {
        return Result.ok(medicalRecordMapper.findByAnimalId(animal_id));
    }

    @PostMapping
    public Result<MedicalRecord> add(@RequestBody MedicalRecord record) {
        medicalRecordMapper.insert(record);
        return Result.ok("新增成功", record);
    }
}
