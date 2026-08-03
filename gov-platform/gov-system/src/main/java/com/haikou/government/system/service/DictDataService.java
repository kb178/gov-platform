package com.haikou.government.system.service;

import com.haikou.government.system.domain.DictData;
import com.haikou.government.system.dto.DictDataDTO;
import com.haikou.government.system.vo.DictDataVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 字典数据表 服务接口
 *
 * @author xhl
 * @since 2026-08-01
 */
public interface DictDataService extends IService<DictData> {

    /**
     * 根据字典类型查询字典数据列表
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<DictDataVO> getDictDataByType(String dictType);

    /**
     * 查询字典数据详情
     *
     * @param dictCode 字典编码
     * @return 字典数据信息
     */
    DictDataVO getDictDataById(Long dictCode);

    /**
     * 新增字典数据
     *
     * @param dictDataDTO 字典数据参数
     * @return 是否成功
     */
    boolean addDictData(DictDataDTO dictDataDTO);

    /**
     * 修改字典数据
     *
     * @param dictDataDTO 字典数据参数
     * @return 是否成功
     */
    boolean updateDictData(DictDataDTO dictDataDTO);

    /**
     * 删除字典数据
     *
     * @param dictCode 字典编码
     * @return 是否成功
     */
    boolean deleteDictData(Long dictCode);
}
