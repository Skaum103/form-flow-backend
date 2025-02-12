package com.example.form_flow_backend.configuration;

import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.UserRepository; // <-- 需要导入你的 UserRepository
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // <-- 需要导入@Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Optional;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@ConditionalOnWebApplication
public class SecurityConfig {
    Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserRepository userRepository; // <-- 注入 UserRepository

    /**
     * Configures the security filter chain for HTTP requests.
     *
     * @param http the HttpSecurity instance
     * @return the configured SecurityFilterChain
     * @throws Exception in case of configuration error
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for testing; enable in production with CSRF token
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/logout",
                                "/auth/session",
                                "/v3/**",
                                "/swagger-ui/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login")
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            String sessionId = request.getSession().getId();

                            // 这里依然使用前端表单提交过来的 "username" 参数
                            String username = request.getParameter("username");

                            // 从数据库查找用户，拿到邮箱
                            Optional<User> userOpt = userRepository.findByUsername(username);
                            String email = userOpt.map(User::getEmail).orElse("");

                            // 拼装 JSON：注意要加逗号、去掉多余逗号，保证合法
                            response.getWriter().write(
                                    "{"
                                            + "\"success\":true,"
                                            + "\"message\":\"Login successful\","
                                            + "\"username\":\"" + (username == null ? "" : username) + "\","
                                            + "\"email\":\"" + (email == null ? "" : email) + "\","
                                            + "\"JSESSIONID\":\"" + sessionId + "\""
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
     * Provides the password encoder bean.
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Ensures passwords are securely hashed
    }
}
