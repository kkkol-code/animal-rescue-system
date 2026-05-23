package com.animalrescue.controller;

import com.animalrescue.common.Result;
import com.animalrescue.service.AdoptionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/adoption")
@CrossOrigin
public class AdoptionController {

    private final AdoptionService adoptionService;

    public AdoptionController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    /**
     * POST /api/adoption/apply
     * 提交领养申请（核心事务接口）
     *
     * 请求体 JSON:
     * {
     *   "real_name": "张三",
     *   "phone": "13800138000",
     *   "id_card": "110101199001011234",
     *   "animal_id": 1001,
     *   "house_condition": "自有住房",
     *   "remark": "备注..."
     * }
     */
    @PostMapping("/apply")
    public Result<?> apply(@RequestBody Map<String, Object> body) {
        try {
            String realName = (String) body.get("real_name");
            String phone = (String) body.get("phone");
            String idCard = (String) body.get("id_card");
            Long animalId = Long.valueOf(body.get("animal_id").toString());
            String houseCondition = (String) body.get("house_condition");
            String remark = (String) body.getOrDefault("remark", "");

            Map<String, Object> result = adoptionService.apply(
                    realName, phone, idCard, animalId, houseCondition, remark);
            return Result.ok("领养申请提交成功", result);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}
