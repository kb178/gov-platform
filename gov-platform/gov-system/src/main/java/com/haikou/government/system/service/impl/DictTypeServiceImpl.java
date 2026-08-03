package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.system.domain.DictData;
import com.haikou.government.system.domain.DictType;
import com.haikou.government.system.dto.DictTypeDTO;
import com.haikou.government.system.mapper.DictDataMapper;
import com.haikou.government.system.mapper.DictTypeMapper;
import com.haikou.government.system.service.DictTypeService;
import com.haikou.government.system.vo.DictTypeVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典类型表 服务实现类
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Service
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    @Autowired
    private DictDataMapper dictDataMapper;

    /**
     * 查询字典类型列表
     */
    @Override
    public List<DictTypeVO> getDictTypeList() {
        List<DictType> list = this.list(new LambdaQueryWrapper<DictType>()
                .orderByAsc(DictType::getDictId));
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询字典类型详情
     */
    @Override
    public DictTypeVO getDictTypeById(Long dictId) {
        DictType dictType = this.getById(dictId);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        return convertToVO(dictType);
    }

    /**
     * 新增字典类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addDictType(DictTypeDTO dictTypeDTO) {
        // 校验字典类型唯一性
        checkDictTypeUnique(dictTypeDTO.getDictType(), null);

        DictType dictType = new DictType();
        BeanUtils.copyProperties(dictTypeDTO, dictType);
        this.save(dictType);

        log.info("新增字典类型成功: dictId={}, dictType={}", dictType.getDictId(), dictType.getDictType());
        return true;
    }

    /**
     * 修改字典类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictType(DictTypeDTO dictTypeDTO) {
        Long dictId = dictTypeDTO.getDictId();
        if (dictId == null) {
            throw new BusinessException("字典类型ID不能为空");
        }

        DictType existing = this.getById(dictId);
        if (existing == null) {
            throw new BusinessException("字典类型不存在");
        }

        // 校验字典类型唯一性（排除自身）
        checkDictTypeUnique(dictTypeDTO.getDictType(), dictId);

        // 如果字典类型变了，需要同步更新字典数据表
        if (!existing.getDictType().equals(dictTypeDTO.getDictType())) {
            dictDataMapper.update(null, new LambdaUpdateWrapper<DictData>()
                    .eq(DictData::getDictType, existing.getDictType())
                    .set(DictData::getDictType, dictTypeDTO.getDictType()));
        }

        DictType dictType = new DictType();
        BeanUtils.copyProperties(dictTypeDTO, dictType);
        this.updateById(dictType);

        log.info("修改字典类型成功: dictId={}", dictId);
        return true;
    }

    /**
     * 删除字典类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictType(Long dictId) {
        DictType dictType = this.getById(dictId);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }

        // 删除字典类型
        this.removeById(dictId);

        // 删除该类型下的所有字典数据
        dictDataMapper.delete(new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictType, dictType.getDictType()));

        log.info("删除字典类型成功: dictId={}, dictType={}", dictId, dictType.getDictType());
        return true;
    }

    /**
     * 校验字典类型唯一性
     */
    private void checkDictTypeUnique(String dictType, Long dictId) {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictType::getDictType, dictType);
        if (dictId != null) {
            wrapper.ne(DictType::getDictId, dictId);
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("字典类型已存在");
        }
    }

    /**
     * 实体转VO
     */
    private DictTypeVO convertToVO(DictType dictType) {
        DictTypeVO vo = new DictTypeVO();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }
}
