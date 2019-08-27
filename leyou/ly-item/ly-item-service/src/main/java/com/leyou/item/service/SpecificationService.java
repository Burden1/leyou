package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;
    /**
     * 根据cid查询规格组
     * @param cid
     * @return
     */
    public List<SpecGroup> querySpecGroupByCid(Long cid){
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        System.out.println(specGroup);
        List<SpecGroup> list = specGroupMapper.select(specGroup);
        if(CollectionUtils.isEmpty(list)){
            log.error("商品规格组查询为空");
            throw new LyException(ExceptionEnum.GROUP_NOT_FOND);
        }
        return list;
    }

    /**
     * 根据组id gid 查
     * cid通过分类id查
     * searching:通过搜索的字段查
     * 查询规格参数
     */
    public List<SpecParam> queryParamsList(Long gid, Long cid, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> paramsList = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(paramsList)){
            throw new LyException(ExceptionEnum.PARAMS_CANNOT_BE_FOND);
        }
        return paramsList;
    }

    /**
     * 通过分类id查询规格组list集合
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupListByCid(Long cid) {
        //1.查询规格组
        List<SpecGroup> specGroups = querySpecGroupByCid(cid);
        //2.查询当前分类下的参数
        List<SpecParam> Params = queryParamsList(null, cid, null);
        //3.先把规格参数变成map。map的key是规格组id map的值是组下的所有参数
        Map<Long,List<SpecParam>> map = new HashMap<>();
        for (SpecParam param:Params){
            //组id在map中不存在就新增一个List
            if (!map.containsKey(param.getGroupId())){
                map.put(param.getGroupId(),new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }
        //4.填充param到group
        for(SpecGroup specGroup :specGroups){
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
