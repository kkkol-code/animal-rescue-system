package com.animalrescue.controller;

import com.animalrescue.common.Result;
import com.animalrescue.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * GET /api/dashboard/stats
     * 返回卡片数据 + 品种分布饼图数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = dashboardService.getStats();
        return Result.ok(data);
    }

    /**
     * GET /api/dashboard/breed-distribution
     * 单独返回品种分布数据（供前端独立刷新饼图）
     */
    @GetMapping("/breed-distribution")
    public Result<?> breedDistribution() {
        Map<String, Object> stats = dashboardService.getStats();
        return Result.ok(stats.get("breedData"));
    }

    /**
     * GET /api/dashboard/monthly-trend
     * 月度趋势数据（暂留占位，后续对接统计表后即可返回真实数据）
     */
    @GetMapping("/monthly-trend")
    public Result<Map<String, Object>> monthlyTrend() {
        // TODO: 对接统计表后返回真实 6 个月趋势
        Map<String, Object> trend = new java.util.LinkedHashMap<>();
        trend.put("months", new String[]{"2025-12","2026-01","2026-02","2026-03","2026-04","2026-05"});
        trend.put("rescued", new int[]{32,28,35,41,37,45});
        trend.put("adopted", new int[]{18,22,20,25,30,28});
        return Result.ok(trend);
    }
}
