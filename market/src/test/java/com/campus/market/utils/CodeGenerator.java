package com.campus.market.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 * 作用：一键生成 Entity, Mapper, Service 代码
 */
public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator
                .create("jdbc:mysql://localhost:3306/campus_flea_market?serverTimezone=Asia/Shanghai", "root", "root")
                .globalConfig(builder -> {
                    builder.author("GraduationTutor") // 设置作者
                            .enableSwagger() // 开启 swagger 模式（可选）
                            .outputDir(System.getProperty("user.dir") + "/src/main/java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.campus.market") // 设置父包名
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .pathInfo(Collections.singletonMap(OutputFile.xml,
                                    System.getProperty("user.dir") + "/src/main/resources/mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("user", "category", "product", "orders", "chat_message", "favorite",
                            "order_review", "quick_reply", "report", "system_config", "activity_topic") // 设置需要生成的表名
                            .entityBuilder()
                            .enableLombok() // 启用 Lombok
                            .enableTableFieldAnnotation() // 启用字段注解
                            .controllerBuilder()
                            .enableRestStyle(); // 启用 REST 风格
                })
                .templateEngine(new VelocityTemplateEngine()) // 使用Velocity模板引擎
                .execute();
    }
}