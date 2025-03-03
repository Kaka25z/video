package com.video.core.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.video.core.entity.User;
import com.video.core.service.impl.user.UserDetailsImpl;
import com.video.core.utils.JwtUtil;
import com.video.core.utils.RedisUtil;

import javax.security.sasl.AuthenticationException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * token 认证过滤器，任何请求访问服务器都会先被这里拦截验证token合法性
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            // 通过开放接口过滤器后，如果没有可解析的token就放行
            filterChain.doFilter(request, response);
            return;
        }

        token = token.substring(7);

        // 解析token
        boolean verifyToken = jwtUtil.verifyToken(token);
        if (!verifyToken) {
            response.addHeader("message", "not login");
            response.setStatus(403);
            return;
        }
        String userId = JwtUtil.getSubjectFromToken(token);
        String role = JwtUtil.getClaimFromToken(token, "role");

        User user = redisUtil.getObject("security:" + role + ":" + userId, User.class);

        if (user == null) {
            response.addHeader("message", "not login");
            response.setStatus(403);
            return;
        }

        UserDetailsImpl loginUser = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 放行
        filterChain.doFilter(request, response);
    }
}
