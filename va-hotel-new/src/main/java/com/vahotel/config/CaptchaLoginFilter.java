package com.vahotel.config;
import jakarta.servlet.*;import jakarta.servlet.http.*;import java.io.IOException;import org.springframework.web.filter.OncePerRequestFilter;
public class CaptchaLoginFilter extends OncePerRequestFilter {
 private final CaptchaService captcha; public CaptchaLoginFilter(CaptchaService c){captcha=c;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  if("POST".equalsIgnoreCase(req.getMethod())&&(req.getServletPath().equals("/login")||req.getServletPath().equals("/register"))&&!captcha.verify(req.getParameter("g-recaptcha-response"),req.getRemoteAddr())){res.sendRedirect(req.getContextPath()+(req.getServletPath().equals("/login")?"/login?captchaError":"/register?captchaError"));return;}chain.doFilter(req,res);
 }
}
