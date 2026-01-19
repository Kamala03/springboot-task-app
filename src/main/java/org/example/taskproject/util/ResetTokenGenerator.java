package org.example.taskproject.util;


import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class ResetTokenGenerator {
    public String resetToken() {
        return UUID.randomUUID().toString();
    }
}
