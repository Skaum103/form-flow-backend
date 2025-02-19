package com.example.form_flow_backend.configuration;

import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@ConditionalOnWebApplication
public class SecurityConfig {

    private final UserRepository userRepository;

    // 如果之前用 @Autowired，也可以用构造注入
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 显式声明一个 CorsConfigurationSource，以便让 Spring Security 正确使用 CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许跨域的来源（确保与你前端实际访问域名一致）
        configuration.setAllowedOrigins(List.of(
                "http://from-flow-fe.us-east-1.elasticbeanstalk.com",
                "http://localhost:3000"
        ));

        // 允许的 HTTP 方法
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));

        // 允许的请求头
        configuration.setAllowedHeaders(List.of("*"));

        // 允许携带认证信息（Cookie 等）
        configuration.setAllowCredentials(true);

        // 如果前端需要读取自定义响应头，可以在这里配置：
        // configuration.addExposedHeader("Set-Cookie");

        // 将该配置应用于所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Configures the security filter chain for HTTP requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 使用自定义的 corsConfigurationSource()
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 测试环境下禁用 CSRF（生产环境请根据需要配置）
                .csrf(AbstractHttpConfigurer::disable)
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
                            String username = request.getParameter("username");

                            Optional<User> userOpt = userRepository.findByUsername(username);
                            String email = userOpt.map(User::getEmail).orElse("");

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
