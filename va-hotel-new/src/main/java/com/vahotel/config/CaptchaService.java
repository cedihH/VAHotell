package com.vahotel.config;
import com.fasterxml.jackson.databind.ObjectMapper;import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import java.net.URI;import java.net.URLEncoder;import java.net.http.*;import java.nio.charset.StandardCharsets;import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Service;
@Service public class CaptchaService {
 private static final Logger log=LoggerFactory.getLogger(CaptchaService.class);
 private final String siteKey; private final String secret; private final boolean enforce; private final ObjectMapper mapper; private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
 public CaptchaService(@Value("${google.recaptcha.site-key:}") String siteKey,@Value("${google.recaptcha.secret-key:}") String secret,@Value("${va.hotel.captcha.enforce:true}") boolean enforce,ObjectMapper mapper){this.siteKey=siteKey==null?"":siteKey.trim();this.secret=secret==null?"":secret.trim();this.enforce=enforce&&isConfigured();this.mapper=mapper;}
 public boolean isEnforced(){return enforce;} public boolean isConfigured(){return !siteKey.isBlank()&&!secret.isBlank();}
 public boolean verify(String token,String ip){if(!enforce)return true;if(token==null||token.isBlank())return false;try{String body="secret="+enc(secret)+"&response="+enc(token)+(ip==null?"":"&remoteip="+enc(ip));var req=HttpRequest.newBuilder(URI.create("https://www.google.com/recaptcha/api/siteverify")).timeout(Duration.ofSeconds(8)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build();var res=client.send(req,HttpResponse.BodyHandlers.ofString());var result=mapper.readTree(res.body());if(res.statusCode()!=200){log.warn("Google reCAPTCHA verification returned HTTP {}.",res.statusCode());return false;}if(!result.path("success").asBoolean(false)){log.warn("Google reCAPTCHA rejected the response token. error-codes={}",result.path("error-codes"));return false;}return true;}catch(Exception e){log.warn("Google reCAPTCHA verification failed: {}",e.getMessage());return false;}}
 private String enc(String v){return URLEncoder.encode(v,StandardCharsets.UTF_8);}
}
