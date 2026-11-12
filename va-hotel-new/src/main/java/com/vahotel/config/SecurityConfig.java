package com.vahotel.config;
import org.springframework.context.annotation.*;import org.springframework.http.HttpMethod;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.security.web.SecurityFilterChain;import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,CaptchaService captcha)throws Exception{
  http.authorizeHttpRequests(a->a.requestMatchers("/","/rooms","/login","/register","/verify-email","/identity","/chatbot","/css/**","/js/**","/images/**","/error").permitAll()
   .requestMatchers("/super-admin/**","/management/customers","/management/customers/**","/management/managers","/management/managers/**").hasRole("SUPER_ADMIN")
   .requestMatchers("/online-manager/**","/management/online/**","/management/reservations","/management/reservations/**").hasAnyRole("ONLINE_MANAGER","SUPER_ADMIN")
   .requestMatchers("/onsite-manager/**","/management/walkins","/management/walkins/**").hasAnyRole("ONSITE_MANAGER","SUPER_ADMIN")
   .requestMatchers("/management/**").hasAnyRole("ONLINE_MANAGER","ONSITE_MANAGER","SUPER_ADMIN")
   .requestMatchers("/customer/**").hasRole("CUSTOMER").anyRequest().authenticated())
   .formLogin(f->f.loginPage("/login").usernameParameter("username").successHandler((req,res,auth)->{String role=auth.getAuthorities().iterator().next().getAuthority();String path=role.equals("ROLE_SUPER_ADMIN")?"/super-admin/dashboard":role.equals("ROLE_ONLINE_MANAGER")?"/online-manager/dashboard":role.equals("ROLE_ONSITE_MANAGER")?"/onsite-manager/dashboard":"/customer/dashboard";res.sendRedirect(req.getContextPath()+path);}).permitAll())
   .logout(l->l.logoutSuccessUrl("/").permitAll());
  http.addFilterBefore(new CaptchaLoginFilter(captcha),UsernamePasswordAuthenticationFilter.class);return http.build();
 }
}
