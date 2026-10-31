package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){

        log.info("新增菜品：{}",dishDTO) ;
        dishService.saveWithFlavor(dishDTO);

        //清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId() ;
        extracted(key);

        return Result.success() ;
    }


    //query方式，不用加rBody
    //TODO query方式是怎么传递参数到dto的。
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult =  dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult) ;
    }


    /**
     * 删除菜品
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除菜品：{}",ids);
        dishService.deleteBatch(ids);

        //删除菜品，直接给所有缓存都清了
        extracted("dish_*");

        return Result.success() ;
    }

    /**
     * 根据id查菜品和口味
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查菜品：{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){

        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //修改菜品，直接给所有缓存都清了

        extracted("dish_*");

        return Result.success() ;
    }

    @PostMapping("status/{status}")
    @ApiOperation("菜品起售停售")
    public Result StatusUpdate(@PathVariable Integer status,Long id){
        dishService.StatusUpdate(status,id) ;

        extracted("dish_*");

        return Result.success();
    }


    /**
     * 删除所有缓存
     */
    private void extracted(String pattern) {
        //修改菜品，直接给所有缓存都清了
        //TODO 这里也算法太粗糙了，改进一下
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

    /**
     * 根据分类ID查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> list( Long categoryId){

        List<Dish> list =  dishService.list(categoryId);
        return Result.success(list) ;
    }


}
