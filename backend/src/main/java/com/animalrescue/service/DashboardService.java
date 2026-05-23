package com.animalrescue.service;

import com.animalrescue.mapper.AnimalMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private final AnimalMapper animalMapper;

    public DashboardService(AnimalMapper animalMapper) {
        this.animalMapper = animalMapper;
    }

    /**
     * 返回看板统计数据：
     *   inStation     — 当前在站动物数（待领养 + 生病中 + 治疗中）
     *   totalRescued  — 累计救助数（全部动物数）
     *   totalAdopted  — 累计领养数
     *   monthlySupply — 本月消耗物资（暂返回固定值，后续对接物资表）
     *   breedData     — 品种分布（饼图数据）
     */
    public Map<String, Object> getStats() {
        int total = animalMapper.countTotal();
        int adopted = animalMapper.countByStatus("adopted");
        int inStation = total - adopted;
        List<AnimalMapper.BreedStat> breedStats = animalMapper.countByBreed();

        List<Map<String, Object>> breedData = new ArrayList<>();
        for (AnimalMapper.BreedStat bs : breedStats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", bs.getName());
            item.put("value", bs.getValue());
            breedData.add(item);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("inStation", inStation);
        stats.put("totalRescued", total);
        stats.put("totalAdopted", adopted);
        stats.put("breedData", breedData);
        return stats;
    }
}
