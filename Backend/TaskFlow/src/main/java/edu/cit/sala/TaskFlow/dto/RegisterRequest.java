package edu.cit.sala.TaskFlow.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String email;
    private String password;
    private String confirmPassword;
    private String fullName;
}