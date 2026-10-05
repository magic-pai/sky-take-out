package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量新增菜品口味
     * @param flavors
     */
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);

    /**
     * 根据菜品id查询口味
     * @param dishId
     * @return
     */
    List<DishFlavor> getByDishId(Long dishId);

    /**
     * 根据菜品id删除口味
     * @param dishId
     */
    void deleteByDishId(Long dishId);

    /**
     * 根据菜品id集合批量删除口味
     * @param dishIds
     */
    void deleteByDishIds(@Param("dishIds") List<Long> dishIds);

}
