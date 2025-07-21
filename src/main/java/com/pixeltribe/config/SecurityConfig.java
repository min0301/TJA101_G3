package com.pixeltribe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity // 明確啟用 Web Security
class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    //    cors設定會覆寫cors.default
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);
        config.addExposedHeader("*");
        config.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
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
                .cors(Customizer.withDefaults()) // 建議使用預設或自訂的 CORS 設定，而不是 disable()

                .authorizeHttpRequests(auth -> auth
                                // 【最重要】首先允許所有 OPTIONS 預檢請求通過
//                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 如果測試完都OK刪掉
                                // 1. 設定公開端點 (任何人都可以訪問)
                                //    - 登入/註冊 API
                                //    - 靜態資源 (css, js, images...)
                                //    - 不需登入就可查看的 GET 請求 (例如: 熱門討論區、文章列表、分類列表)
                                .requestMatchers(

                                        "/api/**",  //暫時全開，記得去下面加入自己的方法
                                        //========靜態公開資源========
                                        "/",
                                        "/assets/**",
                                        "/back-end/**",
                                        "/images/**",
                                        "/js/**",
                                        "/favicon.ico",
                                        "/out-statics/**",
                                        "/templates/**",
                                        "/front-end/**",

                                        "/index.html",
                                        "/indexstatic.html",
                                        "/swagger-ui/**",
                                        "/uploads/**",              //上傳圖片的位置
                                        //========靜態公開資源========
                                        "/webjars/**",
                                        "/css/**",
                                        "/v3/**",
                                        //========公開API========
                                        //========討論區========
                                        "/api/posts/*/messages",    // 查單一文章的留言列表API
                                        "/api/forums",              // 討論區列表API
                                        "/api/forums/*",            // 單一討論區API
                                        "/api/forums/hot",          // 熱門討論區API
                                        "/api/posts/collect/me",     // 取得會員收藏文章列表API
                                        "ws/**",                // WebSocket 端點
                                        //========論壇文章相關API (確保是GET方法)========
                                        "/api/forumposts/all",
                                        "/api/forumpost/{id}",
                                        "/api/forum/{forNo}/posts",
                                        "/api/forumtag/**", // 文章類別
                                        "/api/forum/**",
                                        "/api/forumpost/**",
                                        "/api/forumposts/**",
                                        "/api/category/**",// 查單一討論區類別
                                        "/api/categorys", // 查全部討論區類別
                                        "/api/forumtag", // 查全部文章類別
                                        "/api/posts/{postId}/collect/status",
                                        "/api/posts/{postId}/like/status",
                                        "/api/auth/**",
                                        //===========================================
                                        //========新聞========
                                        "/api/News/admin/redis/create",
                                        "/api/news/image/temp-images",
                                        "/api/News/**",

                                        //========商城========
                                        "/api/orders/**",                 // ← 新增：訂單相關 API
                                        "/api/orderitem/**",                 // ← 新增：前台訂單明細 API
                                        "/api/admin/orderitem/**"           // ← 新增：後台訂單明細 API
                                        //========會員========

                                ).permitAll()

                                // 2. 設定需要登入才能訪問的端點
                                .requestMatchers("/api/cart/**").authenticated()    // 購物車需要認證的設定 (會覆蓋上面的 permitAll)
                                .requestMatchers("/payment/**").permitAll()         //綠界付款才能回調
                                .requestMatchers(HttpMethod.PUT, "/api/forums/*/collect").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/forums/collect/me").authenticated()
//                                .requestMatchers(HttpMethod.GET, "/api/admin/orderitem/**").authenticated()

                                // 3. 設定需要特定權限(角色)的端點，例如管理員
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                                .requestMatchers("/api/admin/orderitem/**").hasRole("ADMIN")

                                // 4. 兜底規則：除了上面允許的，其他所有請求都需要登入
                                .anyRequest().authenticated()
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
