package com.haikou.government.common.mybatis.generator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事项服务代码生成器
 *
 * 用于生成 gov-item 模块的代码（事项分类、事项、表单模板、材料清单等）
 *
 * 使用方法：
 * 1. 先执行建表 SQL（参考 docs/政务一体化平台-数据库设计.md）
 * 2. 运行 main 方法
 * 3. 生成后手动在实体类中添加 extends BaseEntity
 * 4. 需要逻辑删除的表，在实体中手动添加 @TableLogic delFlag 字段
 *
 * @author xhl
 */
public class ItemCodeGenerator {

    // ==================== 配置区（一般不用改）====================

    /** 数据库连接（事项服务独立数据库） */
    private static final String DB_URL = "jdbc:mysql://localhost:3306/gov_item?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "password";

    /** 作者 */
    private static final String AUTHOR = "xhl";

    /** 父包名（事项服务） */
    private static final String PARENT_PACKAGE = "com.haikou.government.item";

    /** 项目根路径（自动获取） */
    private static final String PROJECT_PATH = System.getProperty("user.dir");

    /** 输出目录（事项服务模块） */
    private static final String MODULE_PATH = "/gov-item";

    /** 要生成的表名（事项服务相关表，共6张） */
    private static final List<String> TABLES = Arrays.asList(
            "item_category",           // 事项分类表
            "item_info",               // 事项信息表
            "item_material_template",  // 材料模板表
            "item_material_relation",  // 事项材料关联表
            "item_form_template",      // 表单模板表
            "item_favorite"            // 事项收藏表
    );

    // ==================== 生成区（一般不用改）====================

    public static void main(String[] args) {
        // 输出路径配置（包含完整的包路径）
        Map<OutputFile, String> pathMap = new HashMap<>();
        String basePath = PROJECT_PATH + MODULE_PATH + "/src/main/java/com/haikou/government/item";
        String xmlPath = PROJECT_PATH + MODULE_PATH + "/src/main/resources/mapper";
        pathMap.put(OutputFile.entity, basePath + "/domain");
        pathMap.put(OutputFile.mapper, basePath + "/mapper");
        pathMap.put(OutputFile.xml, xmlPath);
        pathMap.put(OutputFile.service, basePath + "/service");
        pathMap.put(OutputFile.serviceImpl, basePath + "/service/impl");
        pathMap.put(OutputFile.controller, basePath + "/controller");

        FastAutoGenerator.create(DB_URL, DB_USERNAME, DB_PASSWORD)
            // 全局配置
            .globalConfig(builder -> builder
                .author(AUTHOR)
                .outputDir(basePath)
                .disableOpenDir()
            )
            // 包配置
            .packageConfig(builder -> builder
                .parent(PARENT_PACKAGE)
                .entity("domain")
                .mapper("mapper")
                .xml("mapper.xml")
                .service("service")
                .serviceImpl("service.impl")
                .controller("controller")
                .pathInfo(pathMap)
            )
            // 策略配置
            .strategyConfig(builder -> builder
                .addInclude(TABLES)
                // Entity 策略
                .entityBuilder()
                    .superClass("com.haikou.government.common.core.domain.BaseEntity")
                    .enableLombok()
                    .enableTableFieldAnnotation()
                    // 忽略 BaseEntity 中已有的字段（createBy、createTime、updateBy、updateTime）
                    .addIgnoreColumns("create_by", "create_time", "update_by", "update_time")
                    // 自动填充配置
                    .addTableFills(new Column("create_time", FieldFill.INSERT))
                    .addTableFills(new Column("create_by", FieldFill.INSERT))
                    .addTableFills(new Column("update_time", FieldFill.INSERT_UPDATE))
                    .addTableFills(new Column("update_by", FieldFill.INSERT_UPDATE))
                // Mapper 策略
                .mapperBuilder()
                    .enableMapperAnnotation()
                // Service 策略
                .serviceBuilder()
                    .formatServiceFileName("%sService")
                // Controller 策略
                .controllerBuilder()
                    .enableRestStyle()
            )
            .templateEngine(new FreemarkerTemplateEngine())
            .execute();

        System.out.println("========================================");
        System.out.println("事项服务代码生成完成！");
        System.out.println("输出目录：" + basePath);
        System.out.println("========================================");
        System.out.println("生成后的处理步骤：");
        System.out.println("1. 在实体类中添加 extends BaseEntity");
        System.out.println("2. 在 delFlag 字段手动加 @TableLogic 注解（如果有逻辑删除需求）");
        System.out.println("========================================");
    }
}
