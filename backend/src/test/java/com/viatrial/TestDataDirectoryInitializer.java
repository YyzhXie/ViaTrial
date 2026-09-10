package com.viatrial;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

/**
 * 测试数据源隔离（审计项 D-4）。
 *
 * <p>此前测试完全复用生产配置，从 {@code backend/} 执行 {@code mvn test} 会创建/覆盖真实的
 * {@code backend/data/viatrial.db}。这里在上下文刷新<b>之前</b>把 {@code viatrial.data-dir}
 * 指向 {@code target/test-data}，该目录随 {@code mvn clean} 一起清除。</p>
 *
 * <p>通过 {@code SpringBootTest(classes = Main.class, initializers = ...)} 显式注册：
 * 使用 {@code @SpringBootTest} 的自动查找会把测试类自身当成配置类，从而找不到
 * {@code Main} 上的 {@code @MapperScan}/{@code @SpringBootApplication}。</p>
 */
public class TestDataDirectoryInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "viatrial.data-dir=target/test-data");
    }
}
