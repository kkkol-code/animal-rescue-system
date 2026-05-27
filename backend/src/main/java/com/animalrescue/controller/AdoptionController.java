package com.animalrescue.controller;

import com.animalrescue.common.Result;
import com.animalrescue.service.AdoptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     * 双角色分流：
     *   role=owner → 线上申请，状态 pending（待审核）
     *   role=admin → 线下登记，状态 approved（已通过），动物立即变为 adopted
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
            String role = (String) body.getOrDefault("role", "owner");

            Map<String, Object> result = adoptionService.apply(
                    realName, phone, idCard, animalId, houseCondition, remark, role);
            String msg = "admin".equals(role)
                    ? "线下登记成功，动物已直接标记为已领养"
                    : "领养申请提交成功，请等待审核";
            return Result.ok(msg, result);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** GET /api/adoption/applications?status=pending — 待审核列表 */
    @GetMapping("/applications")
    public Result<List<Map<String, Object>>> listApplications(@RequestParam(defaultValue = "pending") String status) {
        return Result.ok(adoptionService.getPendingApplications());
    }

    /** PUT /api/adoption/{id}/approve — 审核通过 */
    @PutMapping("/{id}/approve")
    public Result<?> approve(@PathVariable Long id) {
        try {
            adoptionService.approve(id);
            return Result.ok("审核通过，动物已标记为已领养");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** PUT /api/adoption/{id}/reject — 审核拒绝 */
    @PutMapping("/{id}/reject")
    public Result<?> reject(@PathVariable Long id) {
        try {
            adoptionService.reject(id);
            return Result.ok("已拒绝该申请");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}
