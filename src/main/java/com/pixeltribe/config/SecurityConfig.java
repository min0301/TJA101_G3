package com.pixeltribe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 建議加上這個註解，明確啟用 Web Security
class SecurityConfig {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // 【新增】密碼加密器的 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 因為使用 JWT，關閉 CSRF
//                .cors(cors -> cors.disable())
                .cors(Customizer.withDefaults()) // 建議使用預設或自訂的 CORS 設定，而不是 disable()

                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // 暫時全開
                // 【修改重點】因為是 JWT，Session 管理設為無狀態 (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .sessionManagement(session -> session.disable())
                // 【移除】移除 httpBasic 和 formLogin，它們是基於 Session 的登入方式
                // .httpBasic(Customizer.withDefaults()) <--- 刪除
                // .formLogin(Customizer.withDefaults()); <--- 刪除
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
