package com.animalrescue.service;

import com.animalrescue.entity.Animal;
import com.animalrescue.mapper.AnimalMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AnimalService {

    private final AnimalMapper animalMapper;

    public AnimalService(AnimalMapper animalMapper) {
        this.animalMapper = animalMapper;
    }

    /**
     * 删除动物（级联删除关联的领养申请）
     */
    @Transactional
    public void deleteAnimal(Long id) {
        Animal animal = animalMapper.findById(id);
        if (animal == null) {
            throw new RuntimeException("动物不存在");
        }
        animalMapper.deleteApplicationsByAnimalId(id);
        animalMapper.deleteById(id);
    }

    /**
     * 新增动物，默认状态为待领养
     */
    public Animal addAnimal(Animal animal) {
        animal.setAdoptStatus("pending");
        animalMapper.insert(animal);
        return animal;
    }

    /**
     * 条件分页查询动物列表
     */
    public Map<String, Object> queryList(String name, String adoptStatus, String species,
                                          int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Animal> list = animalMapper.findByCondition(name, adoptStatus, species, offset, pageSize);
        int total = animalMapper.countByCondition(name, adoptStatus, species);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }
}
