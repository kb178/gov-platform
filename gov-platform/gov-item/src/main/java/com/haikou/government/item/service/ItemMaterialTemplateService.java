package com.haikou.government.item.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.item.domain.ItemMaterialTemplate;
import com.baomidou.mybatisplus.extension.service.IService;
import com.haikou.government.item.dto.ItemMaterialTemplateDTO;
import com.haikou.government.item.vo.ItemMaterialTemplateVO;

import java.util.List;

/**
 * 材料模板表（可复用） 服务接口
 *
 * @author xhl
 * @since 2026-08-10
 */
public interface ItemMaterialTemplateService extends IService<ItemMaterialTemplate> {

    /**
     * 分页查询材料模板列表
     *
     * @param page         分页参数
     * @param materialName 材料名称（可选，模糊查询）
     * @param status       状态（可选）
     * @return 材料模板分页列表
     */
    IPage<ItemMaterialTemplateVO> getMaterialPageList(Page<ItemMaterialTemplateVO> page, String materialName, Byte status);

    /**
     * 查询材料模板详情
     *
     * @param materialId 材料ID
     * @return 材料模板详情
     */
    ItemMaterialTemplateVO getMaterialDetail(Long materialId);

    /**
     * 新增材料模板
     *
     * @param dto 材料模板信息
     * @return 材料ID
     */
    Long addMaterial(ItemMaterialTemplateDTO dto);

    /**
     * 修改材料模板
     *
     * @param dto 材料模板信息
     */
    void updateMaterial(ItemMaterialTemplateDTO dto);

    /**
     * 删除材料模板
     *
     * @param materialId 材料ID
     */
    void deleteMaterial(Long materialId);

    /**
     * 查询所有正常状态的材料模板（用于下拉选择）
     *
     * @return 材料模板列表
     */
    List<ItemMaterialTemplateVO> getAllActiveMaterials();
}
