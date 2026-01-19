package org.example.taskproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TaskProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskProjectApplication.class, args);
    }

}
