package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    //1.注入mapper
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 分页查询品牌
     */
    public PageResult<Brand> queryBrandByPageAndSort(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
            // 开始分页
            PageHelper.startPage(page, rows);
            // 过滤
            Example example = new Example(Brand.class);
            //判断搜索条件不为空：按照名字和首字母搜索
        //select * from tb_brand where name like "%key%" or letter='key' order by id desc
            if (!StringUtils.isNullOrEmpty(key)) {
                example.createCriteria().andLike("name", "%" + key + "%")
                        .orEqualTo("letter", key);
            }
            //排序不为空，默认降序
            if (!StringUtils.isNullOrEmpty(sortBy)) {
                // 排序
                String orderByClause = sortBy + (desc ? " DESC" : " ASC");
                example.setOrderByClause(orderByClause);
            }
            // 查询
            Page<Brand> pageInfo = (Page<Brand>) brandMapper.selectByExample(example);
            // 返回结果
            return new PageResult<>(pageInfo.getTotal(), pageInfo);
        }

    /**
     * 新增品牌
     * 1.请求方式：post 请求url：/brand  请求参数：brand对象和商品分类的id cids 返回结果：无返回值
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids){
        //1.调用brandMapper添加方法
        int count = brandMapper.insert(brand);
        //2.判断count 是否为1 ，为1 增加 不为1抛出异常
        if(count !=1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //新增中间表 sql：insert into tb_category_brand values(1,2)
        //查询品牌id是否对应分类id 不对应则保存失败
        for (Long cid :cids){
             count = brandMapper.insertCategoryBrand(cid, brand.getId());
            if(count != 1){
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    /**
     * 处理品牌名称
     */
    public Brand queryById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    /**
     * 根据cid查询品牌
     * @return
     */
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brandList = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brandList;
    }

    /**
     * 根据ids查询品牌
     * @param ids
     */
    public List<Brand> queryBrandByIds(List<Long> ids) {
        List idList = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(idList)){
            throw  new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return idList;
    }
}
