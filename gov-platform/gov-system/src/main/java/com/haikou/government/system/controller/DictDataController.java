package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.dto.DictDataDTO;
import com.haikou.government.system.service.DictDataService;
import com.haikou.government.system.vo.DictDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-01
 */
@Tag(name = "字典数据管理", description = "字典数据增删改查接口")
@RestController
@RequestMapping("/dictData")
public class DictDataController {

    @Autowired
    private DictDataService dictDataService;

    /**
     * 根据字典类型查询字典数据列表
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @Operation(summary = "查询字典数据列表", description = "根据字典类型获取字典数据列表")
    @GetMapping("/type/{dictType}")
    public R<List<DictDataVO>> listByType(@PathVariable String dictType) {
        List<DictDataVO> list = dictDataService.getDictDataByType(dictType);
        return R.ok(list);
    }

    /**
     * 查询字典数据详情
     *
     * @param dictCode 字典编码
     * @return 字典数据信息
     */
    @Operation(summary = "查询字典数据详情", description = "根据字典编码查询详情")
    @GetMapping("/{dictCode}")
    public R<DictDataVO> getById(@PathVariable Long dictCode) {
        DictDataVO vo = dictDataService.getDictDataById(dictCode);
        return R.ok(vo);
    }

    /**
     * 新增字典数据
     *
     * @param dictDataDTO 字典数据参数
     * @return 是否成功
     */
    @Operation(summary = "新增字典数据", description = "添加新字典数据")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody DictDataDTO dictDataDTO) {
        boolean result = dictDataService.addDictData(dictDataDTO);
        return R.ok(result);
    }

    /**
     * 修改字典数据
     *
     * @param dictDataDTO 字典数据参数
     * @return 是否成功
     */
    @Operation(summary = "修改字典数据", description = "修改字典数据信息")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody DictDataDTO dictDataDTO) {
        boolean result = dictDataService.updateDictData(dictDataDTO);
        return R.ok(result);
    }

    /**
     * 删除字典数据
     *
     * @param dictCode 字典编码
     * @return 是否成功
     */
    @Operation(summary = "删除字典数据", description = "删除字典数据")
    @DeleteMapping("/{dictCode}")
    public R<Boolean> delete(@PathVariable Long dictCode) {
        boolean result = dictDataService.deleteDictData(dictCode);
        return R.ok(result);
    }
}
