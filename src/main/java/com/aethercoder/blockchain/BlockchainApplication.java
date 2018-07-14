package com.aethercoder.blockchain;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.client.RestTemplate;
import com.aethercoder.basic.config.rest.RestTemplateCust;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RefreshScope
@EnableDiscoveryClient
@EnableAutoConfiguration(exclude = {
		org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration.class})
public class BlockchainApplication {

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplateCust();
		return restTemplate;
	}
	public static void main(String[] args) {
		SpringApplication.run(BlockchainApplication.class, args);
	}
}
