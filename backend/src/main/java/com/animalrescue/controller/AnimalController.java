package com.animalrescue.controller;

import com.animalrescue.common.Result;
import com.animalrescue.service.AnimalService;
import com.animalrescue.mapper.AnimalMapper;
import com.animalrescue.entity.Animal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/animals")
@CrossOrigin
public class AnimalController {

    private final AnimalService animalService;
    private final AnimalMapper animalMapper;

    public AnimalController(AnimalService animalService, AnimalMapper animalMapper) {
        this.animalService = animalService;
        this.animalMapper = animalMapper;
    }

    /**
     * POST /api/animals
     * 新增动物，status 默认 pending
     */
    @PostMapping
    public Result<?> add(@RequestBody Animal animal) {
        try {
            Animal saved = animalService.addAnimal(animal);
            return Result.ok("新增成功", saved);
        } catch (Exception e) {
            return Result.fail("新增失败: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/animals/{id}
     * 删除动物（级联删除关联领养申请）
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            animalService.deleteAnimal(id);
            return Result.ok("删除成功");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * PUT /api/animals/{id}/status
     * 管理员更改动物健康状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || !status.matches("pending|sick|treating")) {
            return Result.fail("无效的状态值");
        }
        animalMapper.updateStatus(id, status);
        return Result.ok("状态已更新");
    }

    /**
     * GET /api/animals/{id}
     * 获取单只动物详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Animal animal = animalMapper.findById(id);
        if (animal == null) {
            return Result.fail("动物不存在");
        }
        return Result.ok(animal);
    }

    /**
     * GET /api/animals
     * 支持 name 模糊查询、adopt_status 筛选、species 筛选、分页
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String adopt_status,
            @RequestParam(required = false) String species,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {

        Map<String, Object> data = animalService.queryList(name, adopt_status, species, page, page_size);
        return Result.ok(data);
    }
}
