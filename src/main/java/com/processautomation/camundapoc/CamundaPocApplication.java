package com.processautomation.camundapoc;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import io.camunda.zeebe.spring.client.lifecycle.ZeebeClientLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootApplication
@EnableZeebeClient
@Deployment(resources="classpath:ip-management-bpmn.bpmn")
public class CamundaPocApplication  {
    private final static Logger LOG = LoggerFactory.getLogger(CamundaPocApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(CamundaPocApplication.class, args);
    }

}
