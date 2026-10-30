package com.haikou.government.system.service;

import com.haikou.government.system.domain.DictType;
import com.haikou.government.system.dto.DictTypeDTO;
import com.haikou.government.system.vo.DictTypeVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 字典类型表 服务接口
 *
 * @author xhl
 * @since 2026-08-01
 */
public interface DictTypeService extends IService<DictType> {

    /**
     * 查询字典类型列表
     *
     * @return 字典类型列表
     */
    List<DictTypeVO> getDictTypeList();

    /**
     * 查询字典类型详情
     *
     * @param dictId 字典类型ID
     * @return 字典类型信息
     */
    DictTypeVO getDictTypeById(Long dictId);

    /**
     * 新增字典类型
     *
     * @param dictTypeDTO 字典类型参数
     * @return 是否成功
     */
    boolean addDictType(DictTypeDTO dictTypeDTO);

    /**
     * 修改字典类型
     *
     * @param dictTypeDTO 字典类型参数
     * @return 是否成功
     */
    boolean updateDictType(DictTypeDTO dictTypeDTO);

    /**
     * 删除字典类型
     *
     * @param dictId 字典类型ID
     * @return 是否成功
     */
    boolean deleteDictType(Long dictId);
}
