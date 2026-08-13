package com.haikou.government.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.item.domain.ItemCategory;
import com.haikou.government.item.domain.ItemInfo;
import com.haikou.government.item.dto.ItemInfoDTO;
import com.haikou.government.item.mapper.ItemCategoryMapper;
import com.haikou.government.item.mapper.ItemInfoMapper;
import com.haikou.government.item.service.ItemInfoService;
import com.haikou.government.item.vo.ItemInfoVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事项信息表 服务实现类
 *
 * @author xhl
 * @since 2026-08-10
 */
@Slf4j
@Service
public class ItemInfoServiceImpl extends ServiceImpl<ItemInfoMapper, ItemInfo> implements ItemInfoService {

    @Autowired
    private ItemCategoryMapper categoryMapper;

    /**
     * 分页查询事项列表
     */
    @Override
    public IPage<ItemInfoVO> getItemPageList(Page<ItemInfoVO> page, Long categoryId, String itemName, Byte status) {
        // 构建查询条件
        LambdaQueryWrapper<ItemInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, ItemInfo::getCategoryId, categoryId)
                .like(StringUtils.hasText(itemName), ItemInfo::getItemName, itemName)
                .eq(status != null, ItemInfo::getStatus, status)
                .orderByAsc(ItemInfo::getSortNum)
                .orderByDesc(ItemInfo::getCreateTime);

        // 分页查询
        Page<ItemInfo> itemPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<ItemInfo> result = this.page(itemPage, wrapper);

        // 转换为VO
        Page<ItemInfoVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ItemInfoVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询事项详情
     */
    @Override
    public ItemInfoVO getItemDetail(Long itemId) {
        ItemInfo item = this.getById(itemId);
        if (item == null) {
            throw new BusinessException("事项不存在");
        }
        return convertToVO(item);
    }

    /**
     * 新增事项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addItem(ItemInfoDTO dto) {
        // 校验事项编码唯一性
        checkItemCodeUnique(dto.getItemCode(), null);

        // 校验分类是否存在
        ItemCategory category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("事项分类不存在");
        }

        ItemInfo item = new ItemInfo();
        BeanUtils.copyProperties(dto, item);

        // 默认状态为草稿
        if (item.getStatus() == null) {
            item.setStatus((byte) 0);
        }
        // 默认不支持在线办理
        if (item.getSupportOnline() == null) {
            item.setSupportOnline((byte) 0);
        }
        // 默认排序号
        if (item.getSortNum() == null) {
            item.setSortNum(0);
        }

        this.save(item);

        log.info("新增事项成功: itemId={}, itemName={}", item.getItemId(), item.getItemName());
        return item.getItemId();
    }

    /**
     * 修改事项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(ItemInfoDTO dto) {
        Long itemId = dto.getItemId();
        if (itemId == null) {
            throw new BusinessException("事项ID不能为空");
        }

        ItemInfo existing = this.getById(itemId);
        if (existing == null) {
            throw new BusinessException("事项不存在");
        }

        // 校验事项编码唯一性（排除自身）
        checkItemCodeUnique(dto.getItemCode(), itemId);

        // 校验分类是否存在
        if (dto.getCategoryId() != null) {
            ItemCategory category = categoryMapper.selectById(dto.getCategoryId());
            if (category == null) {
                throw new BusinessException("事项分类不存在");
            }
        }

        // 已发布的事项不能直接修改关键信息（需要先下线）
        if (existing.getStatus() != null && existing.getStatus() == 1) {
            throw new BusinessException("已发布的事项不能修改，请先下线");
        }

        ItemInfo item = new ItemInfo();
        BeanUtils.copyProperties(dto, item);
        this.updateById(item);

        log.info("修改事项成功: itemId={}", itemId);
    }

    /**
     * 删除事项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long itemId) {
        ItemInfo item = this.getById(itemId);
        if (item == null) {
            throw new BusinessException("事项不存在");
        }

        // 已发布的事项不能删除
        if (item.getStatus() != null && item.getStatus() == 1) {
            throw new BusinessException("已发布的事项不能删除，请先下线");
        }

        this.removeById(itemId);
        log.info("删除事项成功: itemId={}, itemName={}", itemId, item.getItemName());
    }

    /**
     * 发布事项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishItem(Long itemId) {
        ItemInfo item = this.getById(itemId);
        if (item == null) {
            throw new BusinessException("事项不存在");
        }

        // 只有草稿状态的事项才能发布
        if (item.getStatus() != null && item.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的事项才能发布");
        }

        ItemInfo update = new ItemInfo();
        update.setItemId(itemId);
        update.setStatus((byte) 1);
        this.updateById(update);

        log.info("发布事项成功: itemId={}, itemName={}", itemId, item.getItemName());
    }

    /**
     * 下线事项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineItem(Long itemId) {
        ItemInfo item = this.getById(itemId);
        if (item == null) {
            throw new BusinessException("事项不存在");
        }

        // 只有已发布的事项才能下线
        if (item.getStatus() == null || item.getStatus() != 1) {
            throw new BusinessException("只有已发布的事项才能下线");
        }

        ItemInfo update = new ItemInfo();
        update.setItemId(itemId);
        update.setStatus((byte) 2);
        this.updateById(update);

        log.info("下线事项成功: itemId={}, itemName={}", itemId, item.getItemName());
    }

    /**
     * 校验事项编码唯一性
     */
    private void checkItemCodeUnique(String itemCode, Long itemId) {
        if (!StringUtils.hasText(itemCode)) {
            return;
        }
        LambdaQueryWrapper<ItemInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemInfo::getItemCode, itemCode);
        if (itemId != null) {
            wrapper.ne(ItemInfo::getItemId, itemId);
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("事项编码已存在");
        }
    }

    /**
     * 实体转VO（包含分类名称等关联信息）
     */
    private ItemInfoVO convertToVO(ItemInfo item) {
        ItemInfoVO vo = new ItemInfoVO();
        BeanUtils.copyProperties(item, vo);

        // 查询分类名称
        if (item.getCategoryId() != null) {
            ItemCategory category = categoryMapper.selectById(item.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }

        // 状态文本
        vo.setStatusText(getStatusText(item.getStatus()));

        return vo;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Byte status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "草稿";
            case 1:
                return "已发布";
            case 2:
                return "已下线";
            default:
                return "未知";
        }
    }
}
