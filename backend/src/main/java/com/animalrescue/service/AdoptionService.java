package com.animalrescue.service;

import com.animalrescue.entity.Adopter;
import com.animalrescue.entity.AdoptionApplication;
import com.animalrescue.entity.Animal;
import com.animalrescue.mapper.AdopterMapper;
import com.animalrescue.mapper.AdoptionApplicationMapper;
import com.animalrescue.mapper.AnimalMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdoptionService {

    private final AdopterMapper adopterMapper;
    private final AdoptionApplicationMapper applicationMapper;
    private final AnimalMapper animalMapper;

    public AdoptionService(AdopterMapper adopterMapper,
                           AdoptionApplicationMapper applicationMapper,
                           AnimalMapper animalMapper) {
        this.adopterMapper = adopterMapper;
        this.applicationMapper = applicationMapper;
        this.animalMapper = animalMapper;
    }

    /**
     * 提交领养申请（核心事务）
     *
     * @param realName       领养人姓名
     * @param phone          电话
     * @param idCard         身份证号
     * @param animalId       心仪动物 ID
     * @param houseCondition 住房情况
     * @param remark         备注
     */
    @Transactional
    public Map<String, Object> apply(String realName, String phone, String idCard,
                                      Long animalId, String houseCondition, String remark) {
        // ========== 1. 校验动物状态 ==========
        Animal animal = animalMapper.findById(animalId);
        if (animal == null) {
            throw new RuntimeException("动物不存在");
        }
        if (!"pending".equals(animal.getAdoptStatus())) {
            throw new RuntimeException("该动物当前不可领养（状态：" + animal.getAdoptStatus() + "）");
        }

        // ========== 2. 查询或新增领养人 ==========
        Adopter adopter = adopterMapper.findByIdCard(idCard);
        if (adopter == null) {
            adopter = new Adopter();
            adopter.setRealName(realName);
            adopter.setPhone(phone);
            adopter.setIdCard(idCard);
            adopter.setHouseCondition(houseCondition);
            adopterMapper.insert(adopter); // 自增主键自动回填 adopterId
        }

        // ========== 3. 插入领养申请 ==========
        AdoptionApplication app = new AdoptionApplication();
        app.setAdopterId(adopter.getAdopterId());
        app.setAnimalId(animalId);
        app.setApplyDate(LocalDate.now().toString());
        app.setStatus("pending");
        app.setRemark(remark);
        applicationMapper.insert(app);

        // ========== 4.（可选）动物状态变更为"申请中" ==========
        animalMapper.updateStatus(animalId, "adopted");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("applicationId", app.getApplicationId());
        result.put("adopterId", adopter.getAdopterId());
        result.put("animalId", animalId);
        result.put("status", "pending");
        return result;
    }
}
