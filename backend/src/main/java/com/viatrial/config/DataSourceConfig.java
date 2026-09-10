package com.viatrial.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据源配置。
 *
 * <p>数据库 URL 必须来自 {@link DataDirectoryResolver} 解析后的<b>绝对路径</b>（审计项 D-2）。
 * 这里刻意用 Java 构造 {@link HikariDataSource}，而不是在 YAML 里写
 * {@code jdbc:sqlite:${app.data-dir}/viatrial.db}：</p>
 *
 * <ul>
 *   <li>{@code app.data-dir} 是运行期由 {@code DataDirectoryResolver} 写入的环境属性，
 *       而 {@code spring.datasource.hikari.*} 是在数据源 bean 初始化时才解析的。
 *       实测两者存在时序缝隙，占位符可能原样传给 SQLite，
 *       表现为启动即 {@code SQLITE_CANTOPEN: unable to open database file}。</li>
 *   <li>在 Java 里直接取值可以彻底消除这个不确定性，也让"URL 只有一个来源"成为编译期事实。</li>
 * </ul>
 */
@Configuration
public class DataSourceConfig {

    /**
     * SQLite 是单写者模型，单连接可规避 {@code database is locked}，代价是写操作串行（审计项 B-4）。
     */
    private static final int MAXIMUM_POOL_SIZE = 1;

    @Bean
    public DataSource dataSource(DataDirectoryResolver dataDirectoryResolver) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("viatrial-pool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl(dataDirectoryResolver.getDatabaseUrl());
        config.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        config.setMinimumIdle(MAXIMUM_POOL_SIZE);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        // 外键开关是连接级且默认关闭，必须对池中每个连接显式开启（审计项 D-5）。
        config.setConnectionInitSql("PRAGMA foreign_keys=ON");
        return new HikariDataSource(config);
    }
}
