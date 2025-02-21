package com.example.form_flow_backend.configuration;

import com.example.form_flow_backend.model.Session;
import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.SessionRepository;
import com.example.form_flow_backend.repository.UserRepository;
import com.example.form_flow_backend.service.SessionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// 关键：这里用的是 MVC 下的 UrlBasedCorsConfigurationSource
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@ConditionalOnWebApplication
public class SecurityConfig {

    private final UserRepository userRepository;
    private final SessionService sessionService;

    public SecurityConfig(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    /**
     * 让 Spring Security 在跨域时使用此配置，确保携带Cookie
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 这里写允许跨域的源，确保和前端实际访问的域名一致
        configuration.setAllowedOrigins(List.of(
                "http://from-flow-fe.us-east-1.elasticbeanstalk.com",
                "http://localhost:3000"
        ));

        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        // 允许跨域带 Cookie
        configuration.setAllowCredentials(true);

        // 如果需要让前端能读取自定义响应头（如 "Set-Cookie"），可在此添加：
        // configuration.addExposedHeader("Set-Cookie");

        // 将上述配置应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Security 核心过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 使用我们自定义的 corsConfigurationSource()
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 测试环境下关闭 CSRF（生产环境请谨慎处理）
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 放行以下端点
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/logout",
                                "/auth/session",
                                "/v3/**",
                                "/swagger-ui/**",
                                "/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login")
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            String username = request.getParameter("username");
                            Optional<User> userOpt = userRepository.findByUsername(username);
                            Session session = sessionService.createSession(username);
                            response.getWriter().write(
                                    "{"
                                            + "\"message\":\"Login successful\","
                                            + "\"username\":\"" + username + "\","
                                            + "\"sessionToken\":\"" + session.getSessionToken() + "\""
                                            + "}"
                            );
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write(
                                    "{\"success\":false,\"message\":\"Invalid credentials\"}"
                            );
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String sessionToken = request.getParameter("sessionToken");
                            sessionService.deleteSession(sessionToken);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"success\":true,\"message\":\"Logout successful\"}"
                            );
                        })
                        .permitAll()
                );

        return http.build();
    }

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
