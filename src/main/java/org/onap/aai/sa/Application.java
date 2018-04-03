package org.onap.aai.sa;

import org.eclipse.jetty.util.security.Password;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import java.util.HashMap;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {


    public static void main(String[] args) {

        String keyStorePassword = System.getProperty("KEY_STORE_PASSWORD");
        if(keyStorePassword==null || keyStorePassword.isEmpty()){
            throw new RuntimeException("Env property KEY_STORE_PASSWORD not set");
        }
        HashMap<String, Object> props = new HashMap<>();
        props.put("server.ssl.key-store-password", Password.deobfuscate(keyStorePassword));
        new Application().configure(new SpringApplicationBuilder (Application.class).properties(props)).run(args);
    }
}
