package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper ;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper ;

    @Override
    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO){

        Dish dish = new Dish();

        //copy,前提是class中属性命名必须一致
        BeanUtils.copyProperties(dishDTO,dish);

        //向表插入一条数据
        dishMapper.insert(dish);

        //获取id
        Long dishId = dish.getId();

        //口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if(flavors != null && flavors.size() > 0){

            //先注入id,lamda表达式
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });

            //插入数据,批量插入
            dishFlavorMapper.insertBatch(flavors);

        }

    }


    /**
     * 菜品分页查询
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        //TODO 这个分页插件什么作用，怎么起到的作用
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize()) ;
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品删除
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {

        //检查是否停售
        for(Long id:ids){
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                //不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //检查套餐是否有关联,setmealdish表
        //新map
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds != null && !setmealIds.isEmpty() ){
            //不能删
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //进行菜品表删除
        for(Long id :ids) {
            dishMapper.deleteById( id );
            //TODO 进行口味表删除 , 可以优化
            dishFlavorMapper.deleteById( id );
        }


    }
}
