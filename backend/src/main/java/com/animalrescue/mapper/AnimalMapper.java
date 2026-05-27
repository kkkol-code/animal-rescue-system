package com.animalrescue.mapper;

import com.animalrescue.entity.Animal;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnimalMapper {

    /** 按条件分页查询 */
    List<Animal> findByCondition(@Param("name") String name,
                                 @Param("adoptStatus") String adoptStatus,
                                 @Param("species") String species,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    /** 按条件统计总数 */
    int countByCondition(@Param("name") String name,
                         @Param("adoptStatus") String adoptStatus,
                         @Param("species") String species);

    /** 按领养状态统计数量 */
    @Select("SELECT COUNT(*) FROM animals WHERE adopt_status = #{status}")
    int countByStatus(@Param("status") String status);

    /** 全部动物总数 */
    @Select("SELECT COUNT(*) FROM animals")
    int countTotal();

    /** 按种类分组统计（用于饼图） */
    @Select("SELECT breed AS name, COUNT(*) AS value FROM animals " +
            "GROUP BY breed ORDER BY value DESC")
    List<BreedStat> countByBreed();

    /** 按 ID 查询 */
    @Select("SELECT * FROM animals WHERE animal_id = #{id}")
    Animal findById(@Param("id") Long id);

    /** 新增动物 */
    @Insert("INSERT INTO animals (name, species, breed, gender, adopt_status, entry_date, vaccination) " +
            "VALUES (#{name}, #{species}, #{breed}, #{gender}, #{adoptStatus}, #{entryDate}, #{vaccination})")
    @Options(useGeneratedKeys = true, keyProperty = "animalId")
    int insert(Animal animal);

    /** 删除动物关联的领养申请 */
    @Delete("DELETE FROM adoption_applications WHERE animal_id = #{animalId}")
    int deleteApplicationsByAnimalId(@Param("animalId") Long animalId);

    /** 删除动物 */
    @Delete("DELETE FROM animals WHERE animal_id = #{id}")
    int deleteById(@Param("id") Long id);

    /** 更新领养状态 */
    @Update("UPDATE animals SET adopt_status = #{status} WHERE animal_id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /** 内部类：品种统计结果 */
    class BreedStat {
        private String name;
        private int value;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}
