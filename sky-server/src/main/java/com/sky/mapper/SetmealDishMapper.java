package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id集合查询套餐关联数量
     * @param dishIds
     * @return
     */
    @Select("<script>" +
            "select count(*) from setmeal_dish where dish_id in " +
            "<foreach collection='dishIds' item='dishId' open='(' separator=',' close=')'>" +
            "#{dishId}" +
            "</foreach>" +
            "</script>")
    Integer countByDishIds(@Param("dishIds") List<Long> dishIds);

}
