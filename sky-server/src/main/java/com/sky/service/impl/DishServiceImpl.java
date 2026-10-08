package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
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

    @Autowired
    private DishService dishService ;

    private SetmealMapper setmealMapper ;

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

//        //进行菜品表删除
//        for(Long id :ids) {
//            dishMapper.deleteById( id );
//
//            dishFlavorMapper.deleteById( id );
//        }
        //进行菜品表批量删除
        dishMapper.deleteByIds( ids );

        dishFlavorMapper.deleteByIds( ids );

    }

    /**
     * 根据id查菜品和口味
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {

        //分两步来查，先查dish表，然后查对应口味表
        Dish dish =  dishMapper.getById(id);

        //口味表
        List<DishFlavor> dishFlavors =  dishFlavorMapper.getByDishId(id);

        //封装到vo
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 修改菜品和关联的口味
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        //修改菜品表基本信息
        dishMapper.update(dish);

        //删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //重新插入口味数据，有可能新增的口味，这也代表着dishid没传过来，所以依旧反射
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if(flavors != null && flavors.size() > 0){

            //先注入id,lamda表达式
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });

            //插入数据,批量插入
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    @Override
    @Transactional
    public void StatusUpdate(Integer status, Long id) {
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();

        dishMapper.update(dish);

        //如果停售，还要把套餐停售
        if(status == StatusConstant.DISABLE){
            List<Long> dishIds = new ArrayList<>() ;
            dishIds.add(id);

            //sql语句
            //select setmeal_id from setmeal_dish where id in (?,?,?)

            //获取套餐id
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds) ;

            //重构套餐id
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }


        }
    }

    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .status(StatusConstant.ENABLE)
                .categoryId(categoryId)
                .build();

        return dishMapper.list(dish);
    }


}
