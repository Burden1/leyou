package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService {
    //注入mapper
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 通过pid查询分类
     * @param pid
     * @return
     */
    public List<Category> queryCategoryItemByPid(Long pid){
        //1.创建Category对象
        Category c = new Category();
        //2.设置pid
        c.setParentId(pid);
        //2.调用mapper 的方法
        List<Category> list = categoryMapper.select(c);
        //3.判断list是否位空
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORRY_NOT_FOND);
        }
        return list;
    }

    /**
     * 处理分类名称
     */
    public List<Category> queryByIds(List<Long> ids){
        List<Category> list = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(list)){
            return (List<Category>) new LyException(ExceptionEnum.CATEGORRY_NOT_FOND);
        }
        return list;
    }
}
