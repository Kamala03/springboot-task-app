package org.example.taskproject.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.taskproject.enums.UserStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDtoResponse {
    private String username;
    private String email;
    private UserStatus userStatus;
}
