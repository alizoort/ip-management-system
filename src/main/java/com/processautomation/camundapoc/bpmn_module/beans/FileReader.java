package com.processautomation.camundapoc.bpmn_module.beans;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class FileReader {
    public Map<String,Object> map ;
    @PostConstruct
    public void postConstruct() throws IOException {
        ClassPathResource staticDataResource = new ClassPathResource("task-screen-association.json");
        String staticDataString = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);
        map = new JSONObject(staticDataString).toMap();
    }
}
