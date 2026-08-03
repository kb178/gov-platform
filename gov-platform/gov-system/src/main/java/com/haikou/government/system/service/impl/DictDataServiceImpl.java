package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.system.domain.DictData;
import com.haikou.government.system.dto.DictDataDTO;
import com.haikou.government.system.mapper.DictDataMapper;
import com.haikou.government.system.service.DictDataService;
import com.haikou.government.system.vo.DictDataVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典数据表 服务实现类
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Service
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataService {

    /**
     * 根据字典类型查询字典数据列表
     */
    @Override
    public List<DictDataVO> getDictDataByType(String dictType) {
        List<DictData> list = this.list(new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictType, dictType)
                .orderByAsc(DictData::getSortNum));
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询字典数据详情
     */
    @Override
    public DictDataVO getDictDataById(Long dictCode) {
        DictData dictData = this.getById(dictCode);
        if (dictData == null) {
            throw new BusinessException("字典数据不存在");
        }
        return convertToVO(dictData);
    }

    /**
     * 新增字典数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addDictData(DictDataDTO dictDataDTO) {
        // 校验字典值唯一性（同类型下）
        checkDictValueUnique(dictDataDTO.getDictType(), dictDataDTO.getDictValue(), null);

        DictData dictData = new DictData();
        BeanUtils.copyProperties(dictDataDTO, dictData);
        this.save(dictData);

        log.info("新增字典数据成功: dictCode={}, dictType={}, dictLabel={}",
                dictData.getDictCode(), dictData.getDictType(), dictData.getDictLabel());
        return true;
    }

    /**
     * 修改字典数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictData(DictDataDTO dictDataDTO) {
        Long dictCode = dictDataDTO.getDictCode();
        if (dictCode == null) {
            throw new BusinessException("字典编码不能为空");
        }

        DictData existing = this.getById(dictCode);
        if (existing == null) {
            throw new BusinessException("字典数据不存在");
        }

        // 校验字典值唯一性（同类型下，排除自身）
        checkDictValueUnique(dictDataDTO.getDictType(), dictDataDTO.getDictValue(), dictCode);

        DictData dictData = new DictData();
        BeanUtils.copyProperties(dictDataDTO, dictData);
        this.updateById(dictData);

        log.info("修改字典数据成功: dictCode={}", dictCode);
        return true;
    }

    /**
     * 删除字典数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictData(Long dictCode) {
        DictData dictData = this.getById(dictCode);
        if (dictData == null) {
            throw new BusinessException("字典数据不存在");
        }

        this.removeById(dictCode);
        log.info("删除字典数据成功: dictCode={}", dictCode);
        return true;
    }

    /**
     * 校验字典值唯一性（同类型下）
     */
    private void checkDictValueUnique(String dictType, String dictValue, Long dictCode) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictType)
                .eq(DictData::getDictValue, dictValue);
        if (dictCode != null) {
            wrapper.ne(DictData::getDictCode, dictCode);
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("同类型下字典值已存在");
        }
    }

    /**
     * 实体转VO
     */
    private DictDataVO convertToVO(DictData dictData) {
        DictDataVO vo = new DictDataVO();
        BeanUtils.copyProperties(dictData, vo);
        return vo;
    }
}
