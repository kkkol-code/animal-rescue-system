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
import java.util.List;
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
     * 提交领养申请（双角色分流）
     *
     * @param role owner=线上申请(pending) / admin=线下登记(approved)
     */
    @Transactional
    public Map<String, Object> apply(String realName, String phone, String idCard,
                                      Long animalId, String houseCondition, String remark,
                                      String role) {
        Animal animal = animalMapper.findById(animalId);
        if (animal == null) {
            throw new RuntimeException("动物不存在");
        }
        if (!"pending".equals(animal.getAdoptStatus())) {
            throw new RuntimeException("该动物当前不可领养（状态：" + animal.getAdoptStatus() + "）");
        }

        Adopter adopter = adopterMapper.findByIdCard(idCard);
        if (adopter == null) {
            adopter = new Adopter();
            adopter.setRealName(realName);
            adopter.setPhone(phone);
            adopter.setIdCard(idCard);
            adopter.setHouseCondition(houseCondition);
            adopterMapper.insert(adopter);
        }

        // 分流：管理员直接 approved，宠物主人 pending
        String appStatus = "admin".equals(role) ? "approved" : "pending";

        AdoptionApplication app = new AdoptionApplication();
        app.setAdopterId(adopter.getAdopterId());
        app.setAnimalId(animalId);
        app.setApplyDate(LocalDate.now().toString());
        app.setStatus(appStatus);
        app.setRemark(remark);
        applicationMapper.insert(app);

        // 管理员线下登记：动物直接变为已领养
        if ("admin".equals(role)) {
            animalMapper.updateStatus(animalId, "adopted");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("applicationId", app.getApplicationId());
        result.put("adopterId", adopter.getAdopterId());
        result.put("animalId", animalId);
        result.put("status", appStatus);
        return result;
    }

    /** 获取待审核申请列表 */
    public List<Map<String, Object>> getPendingApplications() {
        return applicationMapper.findPendingApplications();
    }

    /** 审核通过：更新申请状态 + 动物标记为已领养 */
    @Transactional
    public void approve(Long applicationId) {
        AdoptionApplication app = applicationMapper.findById(applicationId);
        if (app == null) throw new RuntimeException("申请不存在");
        applicationMapper.updateStatus(applicationId, "approved");
        animalMapper.updateStatus(app.getAnimalId(), "adopted");
    }

    /** 审核拒绝：仅更新申请状态 */
    @Transactional
    public void reject(Long applicationId) {
        AdoptionApplication app = applicationMapper.findById(applicationId);
        if (app == null) throw new RuntimeException("申请不存在");
        applicationMapper.updateStatus(applicationId, "rejected");
    }
}
