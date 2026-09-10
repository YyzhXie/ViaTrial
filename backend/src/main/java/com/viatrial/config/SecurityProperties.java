package com.viatrial.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 访问控制配置。
 *
 * <p>ViaTrial 是本地单机应用，不提供登录体系；为防止局域网内未授权设备直接调用写接口
 * （审计项 A-1），对状态变更请求（POST/PUT/PATCH/DELETE）叠加令牌校验。</p>
 *
 * <p>默认配置下服务只绑定回环地址（{@code server.address: 127.0.0.1}），外部设备无法
 * 建立连接；令牌是第二道防线，仅在显式放开监听地址时才会真正被远程使用。</p>
 */
@ConfigurationProperties(prefix = "viatrial.security")
public class SecurityProperties {

    /**
     * 是否校验状态变更请求的访问令牌。默认开启。
     */
    private boolean writeTokenEnabled = true;

    /**
     * 访问令牌。留空时由后端在数据目录中生成并持久化。
     */
    private String writeToken = "";

    /**
     * 是否允许跨站来源（Origin/Referer 与本站不一致）发起状态变更请求。默认拒绝。
     */
    private boolean allowCrossSiteWrites = false;

    public boolean isWriteTokenEnabled() {
        return writeTokenEnabled;
    }

    public void setWriteTokenEnabled(boolean writeTokenEnabled) {
        this.writeTokenEnabled = writeTokenEnabled;
    }

    public String getWriteToken() {
        return writeToken == null ? "" : writeToken.trim();
    }

    public void setWriteToken(String writeToken) {
        this.writeToken = writeToken;
    }

    public boolean isAllowCrossSiteWrites() {
        return allowCrossSiteWrites;
    }

    public void setAllowCrossSiteWrites(boolean allowCrossSiteWrites) {
        this.allowCrossSiteWrites = allowCrossSiteWrites;
    }
}
