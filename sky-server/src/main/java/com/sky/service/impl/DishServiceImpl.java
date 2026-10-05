package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Category;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    @Transactional
    @CacheEvict(cacheNames = "dishCache", key = "#dishDTO.categoryId")
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish existingDish = dishMapper.getByName(dishDTO.getName());
        if (existingDish != null) {
            throw new BaseException(MessageConstant.DISH_ALREADY_EXIST);
        }

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        if (dish.getStatus() == null) {
            dish.setStatus(StatusConstant.ENABLE);
        }
        dishMapper.insert(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id查询菜品和口味
     *
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        if (dish == null) {
            return null;
        }

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        Category category = categoryMapper.getById(dish.getCategoryId());
        if (category != null) {
            dishVO.setCategoryName(category.getName());
        }
        dishVO.setFlavors(dishFlavorMapper.getByDishId(id));
        return dishVO;
    }

    /**
     * 修改菜品和口味
     *
     * @param dishDTO
     */
    @Transactional
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish existingDish = dishMapper.getByName(dishDTO.getName());
        if (existingDish != null && !existingDish.getId().equals(dishDTO.getId())) {
            throw new BaseException(MessageConstant.DISH_ALREADY_EXIST);
        }

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Transactional
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BaseException(MessageConstant.DISH_ID_IS_NULL);
        }

        List<Dish> dishes = dishMapper.getByIds(ids);
        for (Dish dish : dishes) {
            if (StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        Integer setmealCount = setmealDishMapper.countByDishIds(ids);
        if (setmealCount > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        dishFlavorMapper.deleteByDishIds(ids);
        dishMapper.deleteByIds(ids);
    }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    @Cacheable(cacheNames = "dishCache", key = "#dish.categoryId")
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);
        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish item : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(item, dishVO);
            dishVO.setFlavors(dishFlavorMapper.getByDishId(item.getId()));
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }

    /**
     * 菜品起售或停售
     *
     * @param status
     * @param id
     */
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }

}
