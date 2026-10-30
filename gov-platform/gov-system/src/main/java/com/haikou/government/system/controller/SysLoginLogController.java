package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.annotation.Log;
import com.haikou.government.system.dto.LoginLogQueryDTO;
import com.haikou.government.system.enums.BusinessType;
import com.haikou.government.system.service.SysLoginLogService;
import com.haikou.government.system.vo.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-01
 */
@Tag(name = "登录日志管理", description = "登录日志查询接口")
@RestController
@RequestMapping("/sysLoginLog")
public class SysLoginLogController {

    @Autowired
    private SysLoginLogService sysLoginLogService;

    /**
     * 分页查询登录日志
     *
     * @param queryDTO 查询参数（包含分页和条件）
     * @return 分页结果
     */
    @Operation(summary = "分页查询登录日志", description = "支持按用户名、状态、时间范围筛选")
    @GetMapping("/page")
    public R<PageResult<LoginLogVO>> page(LoginLogQueryDTO queryDTO) {
        PageResult<LoginLogVO> pageResult = sysLoginLogService.getLoginLogPage(queryDTO);
        return R.ok(pageResult);
    }

    /**
     * 查询登录日志列表（不分页）
     *
     * @return 登录日志列表
     */
    @Operation(summary = "查询登录日志列表", description = "获取所有登录日志（按时间倒序）")
    @GetMapping("/list")
    public R<List<LoginLogVO>> list() {
        List<LoginLogVO> list = sysLoginLogService.getLoginLogList();
        return R.ok(list);
    }

    /**
     * 查询登录日志详情
     *
     * @param infoId 日志ID
     * @return 登录日志信息
     */
    @Operation(summary = "查询登录日志详情", description = "根据日志ID查询详情")
    @GetMapping("/{infoId}")
    public R<LoginLogVO> getById(@PathVariable("infoId") Long infoId) {
        LoginLogVO vo = sysLoginLogService.getLoginLogById(infoId);
        return R.ok(vo);
    }

    /**
     * 清空登录日志
     *
     * @return 是否成功
     */
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @Operation(summary = "清空登录日志", description = "删除所有登录日志（仅管理员）")
    @DeleteMapping("/clean")
    public R<Boolean> clean() {
        boolean result = sysLoginLogService.cleanLoginLog();
        return R.ok(result);
    }
}
