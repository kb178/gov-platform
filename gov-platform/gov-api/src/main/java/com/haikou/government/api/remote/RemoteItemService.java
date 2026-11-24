package com.haikou.government.api.remote;

import com.haikou.government.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * 事项服务远程调用接口
 *
 * @author xhl
 */
@FeignClient(value = "gov-item", contextId = "remoteItemService")
public interface RemoteItemService {

    /**
     * 查询事项配置的材料清单（含必填标记）
     *
     * @param itemId 事项ID
     * @return 材料清单（materialId, materialName, required）
     */
    @GetMapping("/itemMaterialRelation/item/{itemId}")
    R<List<Map<String, Object>>> getMaterialListByItemId(@PathVariable("itemId") Long itemId);
}
