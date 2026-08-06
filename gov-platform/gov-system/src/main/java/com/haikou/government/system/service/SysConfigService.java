package com.haikou.government.system.service;

import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.system.domain.SysConfig;
import com.haikou.government.system.dto.ConfigDTO;
import com.haikou.government.system.dto.ConfigQueryDTO;
import com.haikou.government.system.vo.ConfigVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统参数表 服务接口
 *
 * @author xhl
 * @since 2026-08-01
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 分页查询系统参数
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<ConfigVO> getConfigPage(ConfigQueryDTO queryDTO);

    /**
     * 查询系统参数列表
     *
     * @return 参数列表
     */
    List<ConfigVO> getConfigList();

    /**
     * 查询系统参数详情
     *
     * @param configId 参数ID
     * @return 参数信息
     */
    ConfigVO getConfigById(Long configId);

    /**
     * 根据参数键名获取参数值
     *
     * @param configKey 参数键名
     * @return 参数值
     */
    String getConfigValueByKey(String configKey);

    /**
     * 新增系统参数
     *
     * @param configDTO 参数信息
     * @return 是否成功
     */
    boolean addConfig(ConfigDTO configDTO);

    /**
     * 修改系统参数
     *
     * @param configDTO 参数信息
     * @return 是否成功
     */
    boolean updateConfig(ConfigDTO configDTO);

    /**
     * 删除系统参数
     *
     * @param configId 参数ID
     * @return 是否成功
     */
    boolean deleteConfig(Long configId);
}
