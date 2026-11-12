package com.vahotel.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;import java.net.URLEncoder;import java.net.http.*;import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Service;
@Service public class CaptchaService {
 private final String secret; private final boolean enforce; private final ObjectMapper mapper; private final HttpClient client=HttpClient.newHttpClient();
 public CaptchaService(@Value("${google.recaptcha.secret-key:}") String secret,@Value("${va.hotel.captcha.enforce:false}") boolean enforce,ObjectMapper mapper){this.secret=secret==null?"":secret.trim();this.enforce=enforce;this.mapper=mapper;}
 public boolean isEnforced(){return enforce;} public boolean isConfigured(){return !secret.isBlank();}
 public boolean verify(String token,String ip){if(!enforce)return true;if(!isConfigured()||token==null||token.isBlank())return false;try{String body="secret="+enc(secret)+"&response="+enc(token)+(ip==null?"":"&remoteip="+enc(ip));var req=HttpRequest.newBuilder(URI.create("https://www.google.com/recaptcha/api/siteverify")).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build();var res=client.send(req,HttpResponse.BodyHandlers.ofString());return res.statusCode()==200&&mapper.readTree(res.body()).path("success").asBoolean(false);}catch(Exception e){return false;}}
 private String enc(String v){return URLEncoder.encode(v,StandardCharsets.UTF_8);}
}
