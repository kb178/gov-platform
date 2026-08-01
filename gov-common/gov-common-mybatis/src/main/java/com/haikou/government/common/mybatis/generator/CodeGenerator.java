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
 * MyBatis-Plus 代码生成器（3.5.5 版本）
 *
 * 使用方法：
 * 1. 修改下面的数据库连接信息
 * 2. 修改要生成的表名
 * 3. 运行 main 方法
 *
 * 生成后手动处理：
 * - 实体类添加 extends BaseEntity
 * - delFlag 字段添加 @TableLogic 注解
 *
 * @author gov-platform
 */
public class CodeGenerator {

    // ==================== 配置区（按需修改）====================

    /** 数据库连接 */
    private static final String DB_URL = "jdbc:mysql://localhost:3306/gov_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";

    /** 作者 */
    private static final String AUTHOR = "gov-platform";

    /** 父包名 */
    private static final String PARENT_PACKAGE = "com.haikou.government.system";

    /** 项目根路径（自动获取） */
    private static final String PROJECT_PATH = System.getProperty("user.dir");

    /** 输出目录（根据目标服务修改） */
    private static final String MODULE_PATH = "/gov-system";

    /** 要生成的表名 */
    private static final List<String> TABLES = Arrays.asList(
        "sys_user",
        "sys_role",
        "sys_menu",
        "sys_dept"
    );

    // ==================== 生成区（一般不用改）====================

    public static void main(String[] args) {
        // 输出路径配置
        Map<OutputFile, String> pathMap = new HashMap<>();
        String javaPath = PROJECT_PATH + MODULE_PATH + "/src/main/java";
        String xmlPath = PROJECT_PATH + MODULE_PATH + "/src/main/resources/mapper";
        pathMap.put(OutputFile.entity, javaPath);
        pathMap.put(OutputFile.mapper, javaPath);
        pathMap.put(OutputFile.xml, xmlPath);
        pathMap.put(OutputFile.service, javaPath);
        pathMap.put(OutputFile.serviceImpl, javaPath);
        pathMap.put(OutputFile.controller, javaPath);

        FastAutoGenerator.create(DB_URL, DB_USERNAME, DB_PASSWORD)
            // 全局配置
            .globalConfig(builder -> builder
                .author(AUTHOR)
                .outputDir(javaPath)
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
        System.out.println("代码生成完成！输出目录：" + javaPath);
        System.out.println("提示：生成后在 delFlag 字段手动加 @TableLogic 注解即可");
        System.out.println("========================================");
    }
}
