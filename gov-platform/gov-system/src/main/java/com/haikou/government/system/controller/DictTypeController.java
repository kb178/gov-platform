package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.annotation.Log;
import com.haikou.government.system.dto.DictTypeDTO;
import com.haikou.government.system.enums.BusinessType;
import com.haikou.government.system.service.DictTypeService;
import com.haikou.government.system.vo.DictTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典类型管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-01
 */
@Tag(name = "字典类型管理", description = "字典类型增删改查接口")
@RestController
@RequestMapping("/dictType")
public class DictTypeController {

    @Autowired
    private DictTypeService dictTypeService;

    /**
     * 查询字典类型列表
     *
     * @return 字典类型列表
     */
    @Operation(summary = "查询字典类型列表", description = "获取所有字典类型")
    @GetMapping("/list")
    public R<List<DictTypeVO>> list() {
        List<DictTypeVO> list = dictTypeService.getDictTypeList();
        return R.ok(list);
    }

    /**
     * 查询字典类型详情
     *
     * @param dictId 字典类型ID
     * @return 字典类型信息
     */
    @Operation(summary = "查询字典类型详情", description = "根据字典类型ID查询详情")
    @GetMapping("/{dictId}")
    public R<DictTypeVO> getById(@PathVariable Long dictId) {
        DictTypeVO vo = dictTypeService.getDictTypeById(dictId);
        return R.ok(vo);
    }

    /**
     * 新增字典类型
     *
     * @param dictTypeDTO 字典类型参数
     * @return 是否成功
     */
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增字典类型", description = "添加新字典类型")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody DictTypeDTO dictTypeDTO) {
        boolean result = dictTypeService.addDictType(dictTypeDTO);
        return R.ok(result);
    }

    /**
     * 修改字典类型
     *
     * @param dictTypeDTO 字典类型参数
     * @return 是否成功
     */
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改字典类型", description = "修改字典类型信息")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody DictTypeDTO dictTypeDTO) {
        boolean result = dictTypeService.updateDictType(dictTypeDTO);
        return R.ok(result);
    }

    /**
     * 删除字典类型
     *
     * @param dictId 字典类型ID
     * @return 是否成功
     */
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除字典类型", description = "删除字典类型及关联的字典数据")
    @DeleteMapping("/{dictId}")
    public R<Boolean> delete(@PathVariable Long dictId) {
        boolean result = dictTypeService.deleteDictType(dictId);
        return R.ok(result);
    }
}
