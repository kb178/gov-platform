package com.haikou.government.system.service;

import com.haikou.government.system.domain.SysOperLog;
import com.haikou.government.system.vo.OperLogVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 操作日志表 服务接口
 *
 * @author xhl
 * @since 2026-08-01
 */
public interface SysOperLogService extends IService<SysOperLog> {

    /**
     * 查询操作日志列表
     *
     * @return 操作日志列表
     */
    List<OperLogVO> getOperLogList();

    /**
     * 查询操作日志详情
     *
     * @param operId 日志ID
     * @return 操作日志信息
     */
    OperLogVO getOperLogById(Long operId);

    /**
     * 清空操作日志
     *
     * @return 是否成功
     */
    boolean cleanOperLog();
}
