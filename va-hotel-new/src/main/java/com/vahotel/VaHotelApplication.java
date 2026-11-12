package com.vahotel;
import org.springframework.boot.SpringApplication;import org.springframework.boot.autoconfigure.SpringBootApplication;import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication @EnableScheduling public class VaHotelApplication {public static void main(String[] args){try{java.nio.file.Files.createDirectories(java.nio.file.Path.of("data"));}catch(java.io.IOException e){throw new IllegalStateException(e);}SpringApplication.run(VaHotelApplication.class,args);}}
