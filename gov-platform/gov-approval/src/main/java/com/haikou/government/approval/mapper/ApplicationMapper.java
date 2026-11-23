package com.haikou.government.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haikou.government.approval.domain.Application;
import org.apache.ibatis.annotations.Mapper;

/**
 * 申请单 Mapper
 *
 * @author xhl
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
}
