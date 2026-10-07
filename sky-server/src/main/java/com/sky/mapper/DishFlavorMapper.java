package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);

    /**
     * 删除菜品id对应的口味数据
     * @param dishId
     */
    @Delete("delete  from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 批量删除菜品id对应的口味数据
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据菜品id对应的口味数据
     * @param id
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);
}
