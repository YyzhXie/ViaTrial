package com.viatrial.security;

import com.viatrial.config.DataDirectoryResolver;
import com.viatrial.config.SecurityProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 会话信息接口，供本站前端页面获取访问令牌。
 *
 * <p>若服务显式绑定到非回环地址（{@code server.address} 改为 {@code 0.0.0.0}），
 * 局域网设备也能访问本接口。为避免令牌因此泄露，令牌<b>只对回环来源</b>下发；
 * 远程调用者需要通过文件或环境变量自行获取令牌。</p>
 */
@Tag(name = "系统信息")
@RestController
@RequestMapping("/api/v1/system")
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);

    private final DataDirectoryResolver dataDirectoryResolver;

    private final SecurityProperties securityProperties;

    public SessionController(DataDirectoryResolver dataDirectoryResolver, SecurityProperties securityProperties) {
        this.dataDirectoryResolver = dataDirectoryResolver;
        this.securityProperties = securityProperties;
    }

    @Operation(summary = "获取前端启动所需的会话信息")
    @GetMapping("/session")
    public Map<String, Object> session(HttpServletRequest request) {
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("writeTokenRequired", securityProperties.isWriteTokenEnabled());

        if (!securityProperties.isWriteTokenEnabled() || !isLoopbackRequest(request)) {
            session.put("writeToken", null);
            return session;
        }

        session.put("writeToken", dataDirectoryResolver.getWriteToken());
        return session;
    }

    private boolean isLoopbackRequest(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        if (remoteAddress == null) {
            return false;
        }

        boolean loopback = "127.0.0.1".equals(remoteAddress)
                || "0:0:0:0:0:0:0:1".equals(remoteAddress)
                || "::1".equals(remoteAddress);
        if (!loopback) {
            log.debug("Refusing to hand out the write token to non-loopback client {}", remoteAddress);
        }
        return loopback;
    }
}
