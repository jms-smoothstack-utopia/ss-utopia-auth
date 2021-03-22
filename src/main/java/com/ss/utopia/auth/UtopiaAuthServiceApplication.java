package com.ss.utopia.auth;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class UtopiaAuthServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(UtopiaAuthServiceApplication.class, args);
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Profile("ecs")
  @Bean
  public EurekaInstanceConfigBean eurekaInstanceConfigBean(InetUtils inetUtils)
      throws UnknownHostException {
    var config = new EurekaInstanceConfigBean(inetUtils);
    config.setIpAddress(InetAddress.getLocalHost().getHostAddress());
    config.setNonSecurePort(8089);
    config.setPreferIpAddress(true);
    return config;
  }
}
