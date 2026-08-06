package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.annotation.Log;
import com.haikou.government.system.dto.ConfigDTO;
import com.haikou.government.system.dto.ConfigQueryDTO;
import com.haikou.government.system.enums.BusinessType;
import com.haikou.government.system.service.SysConfigService;
import com.haikou.government.system.vo.ConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统参数管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-01
 */
@Tag(name = "系统参数管理", description = "系统参数增删改查接口")
@RestController
@RequestMapping("/sysConfig")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 分页查询系统参数
     */
    @Operation(summary = "分页查询系统参数", description = "支持按参数名称、参数键名筛选")
    @GetMapping("/page")
    public R<PageResult<ConfigVO>> page(ConfigQueryDTO queryDTO) {
        PageResult<ConfigVO> pageResult = sysConfigService.getConfigPage(queryDTO);
        return R.ok(pageResult);
    }

    /**
     * 查询系统参数列表
     */
    @Operation(summary = "查询系统参数列表", description = "获取所有系统参数")
    @GetMapping("/list")
    public R<List<ConfigVO>> list() {
        List<ConfigVO> list = sysConfigService.getConfigList();
        return R.ok(list);
    }

    /**
     * 查询系统参数详情
     */
    @Operation(summary = "查询系统参数详情", description = "根据参数ID查询详情")
    @GetMapping("/{configId}")
    public R<ConfigVO> getById(@PathVariable("configId") Long configId) {
        ConfigVO vo = sysConfigService.getConfigById(configId);
        return R.ok(vo);
    }

    /**
     * 根据参数键名获取参数值
     */
    @Operation(summary = "根据键名获取参数值", description = "通过参数键名获取对应的参数值")
    @GetMapping("/key/{configKey}")
    public R<String> getValueByKey(@PathVariable("configKey") String configKey) {
        String value = sysConfigService.getConfigValueByKey(configKey);
        return R.ok(value);
    }

    /**
     * 新增系统参数
     */
    @Log(title = "系统参数", businessType = BusinessType.INSERT)
    @Operation(summary = "新增系统参数", description = "添加新系统参数")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody ConfigDTO configDTO) {
        boolean result = sysConfigService.addConfig(configDTO);
        return R.ok(result);
    }

    /**
     * 修改系统参数
     */
    @Log(title = "系统参数", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改系统参数", description = "修改系统参数信息")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody ConfigDTO configDTO) {
        boolean result = sysConfigService.updateConfig(configDTO);
        return R.ok(result);
    }

    /**
     * 删除系统参数
     */
    @Log(title = "系统参数", businessType = BusinessType.DELETE)
    @Operation(summary = "删除系统参数", description = "删除系统参数")
    @DeleteMapping("/{configId}")
    public R<Boolean> delete(@PathVariable("configId") Long configId) {
        boolean result = sysConfigService.deleteConfig(configId);
        return R.ok(result);
    }
}
