package org.example.taskproject.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.taskproject.enums.RoleName;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDtoRequest {
    private RoleName name;
}
