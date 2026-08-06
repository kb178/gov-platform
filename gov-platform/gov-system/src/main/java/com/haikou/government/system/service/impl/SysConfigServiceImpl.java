package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.system.domain.SysConfig;
import com.haikou.government.system.dto.ConfigDTO;
import com.haikou.government.system.dto.ConfigQueryDTO;
import com.haikou.government.system.mapper.SysConfigMapper;
import com.haikou.government.system.service.SysConfigService;
import com.haikou.government.system.vo.ConfigVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统参数表 服务实现类
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    /**
     * 分页查询系统参数
     */
    @Override
    public PageResult<ConfigVO> getConfigPage(ConfigQueryDTO queryDTO) {
        //构造条件，条件由ConfigQueryDTO传递过来
        LambdaQueryWrapper<SysConfig> wrapper = buildQueryWrapper(queryDTO);
        //构造查询条件
        Page<SysConfig> page = this.page(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                wrapper
        );
        // 转换为VO
        List<ConfigVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        // 返回结果
        return new PageResult<>(page.getTotal(), voList, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 查询系统参数列表
     */
    @Override
    public List<ConfigVO> getConfigList() {
        List<SysConfig> list = this.list(new LambdaQueryWrapper<SysConfig>()
                .orderByAsc(SysConfig::getConfigId));
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询系统参数详情
     */
    @Override
    public ConfigVO getConfigById(Long configId) {
        SysConfig config = this.getById(configId);
        if (config == null) {
            throw new BusinessException("系统参数不存在");
        }
        return convertToVO(config);
    }

    /**
     * 根据参数键名获取参数值
     */
    @Override
    public String getConfigValueByKey(String configKey) {
        SysConfig config = this.getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey));
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 新增系统参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addConfig(ConfigDTO configDTO) {
        // 校验参数键名唯一性
        checkConfigKeyUnique(configDTO.getConfigKey(), null);

        SysConfig config = new SysConfig();
        BeanUtils.copyProperties(configDTO, config);
        this.save(config);
        log.info("新增系统参数成功：{}", configDTO.getConfigKey());
        return true;
    }

    /**
     * 修改系统参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(ConfigDTO configDTO) {
        SysConfig config = this.getById(configDTO.getConfigId());
        if (config == null) {
            throw new BusinessException("系统参数不存在");
        }

        // 校验参数键名唯一性（排除自身）
        checkConfigKeyUnique(configDTO.getConfigKey(), configDTO.getConfigId());

        BeanUtils.copyProperties(configDTO, config);
        this.updateById(config);
        log.info("修改系统参数成功：{}", configDTO.getConfigKey());
        return true;
    }

    /**
     * 删除系统参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(Long configId) {
        SysConfig config = this.getById(configId);
        if (config == null) {
            throw new BusinessException("系统参数不存在");
        }
        this.removeById(configId);
        log.info("删除系统参数成功：{}", config.getConfigKey());
        return true;
    }

    /**
     * 校验参数键名唯一性
     */
    private void checkConfigKeyUnique(String configKey, Long excludeId) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        if (excludeId != null) {
            wrapper.ne(SysConfig::getConfigId, excludeId);
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("参数键名'" + configKey + "'已存在");
        }
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<SysConfig> buildQueryWrapper(ConfigQueryDTO queryDTO) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getConfigName())) {
            // 参数名称（模糊查询）
            wrapper.like(SysConfig::getConfigName, queryDTO.getConfigName());
        }

        if (StringUtils.hasText(queryDTO.getConfigKey())) {
            // 参数键名（模糊查询）
            wrapper.like(SysConfig::getConfigKey, queryDTO.getConfigKey());
        }

        wrapper.orderByAsc(SysConfig::getConfigId);
        return wrapper;
    }

    /**
     * 实体转VO
     */
    private ConfigVO convertToVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}
