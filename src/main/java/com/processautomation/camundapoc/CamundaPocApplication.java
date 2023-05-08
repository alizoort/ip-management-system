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

import java.util.Map;

@SpringBootApplication
@EnableZeebeClient
@Deployment(resources="classpath:ip-management-bpmn.bpmn")
public class CamundaPocApplication implements CommandLineRunner {
    private final static Logger LOG = LoggerFactory.getLogger(CamundaPocApplication.class);
    @Autowired
    private ZeebeClientLifecycle client;
    public static void main(String[] args) {
        SpringApplication.run(CamundaPocApplication.class, args);
    }
    @Override
    public void run(final String... args) throws Exception {
        final ProcessInstanceEvent event =
                client
                        .newCreateInstanceCommand()
                        .bpmnProcessId("IPManagementBusinessProcess")
                        .latestVersion()
                        .variables(Map.of("message_content","Hello From Our Automated IP System"))
                        .send()
                        .join();
        LOG.info("Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
                event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
    }

}
