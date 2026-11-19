package com.haikou.government.common.core;

import com.haikou.government.common.core.domain.R;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * R 统一返回对象 单元测试
 *
 * @author gov-platform
 */
@DisplayName("统一返回对象单元测试")
class RTest {

    @Test
    @DisplayName("ok() - 无参成功返回")
    void ok_noArgs() {
        R<Void> r = R.ok();
        assertEquals(200, r.getCode());
        assertEquals("操作成功", r.getMsg());
        assertNull(r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    @DisplayName("ok(data) - 带数据成功返回")
    void ok_withData() {
        R<String> r = R.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("hello", r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    @DisplayName("ok(data, msg) - 带数据和消息成功返回")
    void ok_withDataAndMsg() {
        R<String> r = R.ok("world", "查询成功");
        assertEquals(200, r.getCode());
        assertEquals("world", r.getData());
        assertEquals("查询成功", r.getMsg());
    }

    @Test
    @DisplayName("fail() - 无参失败返回")
    void fail_noArgs() {
        R<Void> r = R.fail();
        assertEquals(500, r.getCode());
        assertEquals("操作失败", r.getMsg());
        assertFalse(r.isSuccess());
    }

    @Test
    @DisplayName("fail(msg) - 带消息失败返回")
    void fail_withMsg() {
        R<Void> r = R.fail("手机号已注册");
        assertEquals(500, r.getCode());
        assertEquals("手机号已注册", r.getMsg());
    }

    @Test
    @DisplayName("fail(code, msg) - 自定义错误码失败返回")
    void fail_withCodeAndMsg() {
        R<Void> r = R.fail(401, "未授权");
        assertEquals(401, r.getCode());
        assertEquals("未授权", r.getMsg());
        assertFalse(r.isSuccess());
    }

    @Test
    @DisplayName("isSuccess - 200返回true，其他返回false")
    void isSuccess() {
        assertTrue(R.ok().isSuccess());
        assertFalse(R.fail().isSuccess());
        assertFalse(R.fail(404, "not found").isSuccess());
    }
}
