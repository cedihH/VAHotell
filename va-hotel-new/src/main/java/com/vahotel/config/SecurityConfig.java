package com.vahotel.config;
import org.springframework.context.annotation.*;import org.springframework.http.HttpMethod;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.security.web.SecurityFilterChain;import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,CaptchaService captcha)throws Exception{
  http.authorizeHttpRequests(a->a.requestMatchers("/","/rooms","/room-photos/**","/login","/register","/verify-email","/identity","/chatbot","/access-denied","/forgot-password","/reset-password","/css/**","/js/**","/images/**","/error").permitAll()
   .requestMatchers(HttpMethod.GET,"/customer/dashboard").permitAll()
   .requestMatchers("/super-admin/**","/management/customers","/management/customers/**","/management/managers","/management/managers/**").hasRole("SUPER_ADMIN")
   .requestMatchers("/management/reservations/*/checkin","/management/reservations/*/checkout").hasAnyRole("ONSITE_MANAGER","SUPER_ADMIN")
   .requestMatchers("/management/reservations","/management/reservations/**").hasRole("SUPER_ADMIN")
   .requestMatchers("/online-manager/**","/management/online/**").hasRole("ONLINE_MANAGER")
   .requestMatchers("/onsite-manager/**","/management/walkins","/management/walkins/**").hasRole("ONSITE_MANAGER")
   .requestMatchers("/management/**").hasAnyRole("ONLINE_MANAGER","ONSITE_MANAGER","SUPER_ADMIN")
   .requestMatchers("/customer/**").hasRole("CUSTOMER").anyRequest().authenticated())
   .formLogin(f->f.loginPage("/login").usernameParameter("username").successHandler((req,res,auth)->{var roles=auth.getAuthorities().stream().map(org.springframework.security.core.GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());String path=roles.contains("ROLE_SUPER_ADMIN")?"/super-admin/dashboard":roles.contains("ROLE_ONLINE_MANAGER")?"/online-manager/dashboard":roles.contains("ROLE_ONSITE_MANAGER")?"/onsite-manager/dashboard":"/customer/dashboard";res.sendRedirect(req.getContextPath()+path);}).permitAll())
   .logout(l->l.logoutSuccessUrl("/").permitAll())
   .exceptionHandling(e->e.accessDeniedPage("/access-denied"));
  http.addFilterBefore(new CaptchaLoginFilter(captcha),UsernamePasswordAuthenticationFilter.class);return http.build();
 }
}
