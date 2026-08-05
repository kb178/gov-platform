package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.annotation.Log;
import com.haikou.government.system.dto.OperLogQueryDTO;
import com.haikou.government.system.enums.BusinessType;
import com.haikou.government.system.service.SysOperLogService;
import com.haikou.government.system.vo.OperLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-01
 */
@Tag(name = "操作日志管理", description = "操作日志查询接口")
@RestController
@RequestMapping("/sysOperLog")
public class SysOperLogController {

    @Autowired
    private SysOperLogService sysOperLogService;

    /**
     * 分页查询操作日志
     *
     * @param queryDTO 查询参数（包含分页和条件）
     * @return 分页结果
     */
    @Operation(summary = "分页查询操作日志", description = "支持按模块标题、操作人、状态、时间范围筛选")
    @GetMapping("/page")
    public R<PageResult<OperLogVO>> page(OperLogQueryDTO queryDTO) {
        PageResult<OperLogVO> pageResult = sysOperLogService.getOperLogPage(queryDTO);
        return R.ok(pageResult);
    }

    /**
     * 查询操作日志列表（不分页）
     *
     * @return 操作日志列表
     */
    @Operation(summary = "查询操作日志列表", description = "获取所有操作日志（按时间倒序）")
    @GetMapping("/list")
    public R<List<OperLogVO>> list() {
        List<OperLogVO> list = sysOperLogService.getOperLogList();
        return R.ok(list);
    }

    /**
     * 查询操作日志详情
     *
     * @param operId 日志ID
     * @return 操作日志信息
     */
    @Operation(summary = "查询操作日志详情", description = "根据日志ID查询详情")
    @GetMapping("/{operId}")
    public R<OperLogVO> getById(@PathVariable("operId") Long operId) {
        OperLogVO vo = sysOperLogService.getOperLogById(operId);
        return R.ok(vo);
    }

    /**
     * 清空操作日志
     *
     * @return 是否成功
     */
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @Operation(summary = "清空操作日志", description = "删除所有操作日志（仅管理员）")
    @DeleteMapping("/clean")
    public R<Boolean> clean() {
        boolean result = sysOperLogService.cleanOperLog();
        return R.ok(result);
    }
}
