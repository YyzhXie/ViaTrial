package com.viatrial.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viatrial.common.ErrorCode;
import com.viatrial.common.Result;
import com.viatrial.config.DataDirectoryResolver;
import com.viatrial.config.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/**
 * 状态变更请求的访问控制（审计项 A-1）。
 *
 * <p>校验顺序：</p>
 * <ol>
 *   <li>同源放行：本站前端页面发起的请求（Origin/Referer 与请求 Host 一致，或本机回环地址）
 *       直接通过，保证本地使用零配置。</li>
 *   <li>令牌校验：其余来源（局域网设备、本机其他进程、跨站页面）必须携带正确令牌，
 *       跨站页面既无令牌也会因校验失败被拒（可关闭 {@code viatrial.security.allow-cross-site-writes}
 *       显式放行）。</li>
 * </ol>
 */
@Component
public class WriteAccessInterceptor implements HandlerInterceptor {

    public static final String TOKEN_HEADER = "X-ViaTrial-Token";

    private static final Set<String> SAFE_METHODS = Set.of(
            HttpMethod.GET.name(),
            HttpMethod.HEAD.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.TRACE.name());

    private static final Logger log = LoggerFactory.getLogger(WriteAccessInterceptor.class);

    private final SecurityProperties securityProperties;

    private final DataDirectoryResolver dataDirectoryResolver;

    private final ObjectMapper objectMapper;

    public WriteAccessInterceptor(SecurityProperties securityProperties,
                                  DataDirectoryResolver dataDirectoryResolver,
                                  ObjectMapper objectMapper) {
        this.securityProperties = securityProperties;
        this.dataDirectoryResolver = dataDirectoryResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (SAFE_METHODS.contains(request.getMethod())) {
            return true;
        }

        if (!securityProperties.isWriteTokenEnabled()) {
            log.warn("Write access control is disabled; rejecting {} {} to avoid unprotected data changes.",
                    request.getMethod(), request.getRequestURI());
            writeError(request, response, HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.UNAUTHORIZED,
                    "写接口访问控制被显式关闭，已拒绝所有状态变更请求。");
            return false;
        }

        if (securityProperties.isAllowCrossSiteWrites() || isSameSite(request)) {
            return true;
        }

        String presented = request.getHeader(TOKEN_HEADER);
        if (matches(presented)) {
            return true;
        }

        log.warn("Rejected unauthorized write request: {} {} origin={} referer={} from {}",
                request.getMethod(), request.getRequestURI(),
                request.getHeader(HttpHeaders.ORIGIN), request.getHeader(HttpHeaders.REFERER),
                request.getRemoteAddr());
        writeError(request, response, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED,
                "缺少或无效的访问令牌，请从 " + dataDirectoryResolver.getWriteTokenFile() + " 读取令牌后重试。");
        return false;
    }

    /**
     * 判断请求是否来自本站（浏览器同源请求，或使用本机回环地址访问的页面）。
     */
    private boolean isSameSite(HttpServletRequest request) {
        String origin = firstHeader(request, HttpHeaders.ORIGIN);
        String referer = firstHeader(request, HttpHeaders.REFERER);
        String source = origin != null ? origin : referer;

        if (source == null) {
            return false;
        }

        String sourceHost = extractHost(source);
        if (sourceHost == null) {
            return false;
        }

        return sourceHost.equals(extractHost(request.getHeader(HttpHeaders.HOST)))
                || isLoopbackHost(sourceHost);
    }

    private String firstHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String extractHost(String url) {
        if (url == null || url.isBlank() || "null".equalsIgnoreCase(url)) {
            return null;
        }

        String candidate = url;
        int schemeIndex = candidate.indexOf("://");
        if (schemeIndex >= 0) {
            candidate = candidate.substring(schemeIndex + 3);
        }

        int pathIndex = candidate.indexOf('/');
        if (pathIndex >= 0) {
            candidate = candidate.substring(0, pathIndex);
        }

        int atIndex = candidate.lastIndexOf('@');
        if (atIndex >= 0) {
            candidate = candidate.substring(atIndex + 1);
        }

        return candidate.toLowerCase();
    }

    private boolean isLoopbackHost(String host) {
        String hostname = host;
        int portIndex = hostname.lastIndexOf(':');
        if (portIndex > 0 && hostname.indexOf(']') < portIndex) {
            hostname = hostname.substring(0, portIndex);
        }

        return "localhost".equals(hostname)
                || "127.0.0.1".equals(hostname)
                || "[::1]".equals(hostname)
                || "::1".equals(hostname);
    }

    private boolean matches(String presented) {
        String expected = dataDirectoryResolver.getWriteToken();
        if (presented == null || expected == null || expected.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(
                presented.trim().getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private void writeError(HttpServletRequest request,
                            HttpServletResponse response,
                            HttpStatus status,
                            ErrorCode errorCode,
                            String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        objectMapper.writeValue(response.getWriter(), Result.fail(errorCode, message));
    }
}
