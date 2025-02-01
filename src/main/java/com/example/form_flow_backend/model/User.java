package com.example.form_flow_backend.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @NotNull
    private int id;

    @NotNull
    @Size(min = 1, max = 20)
    private String username;

    @NotNull
    @Size(min = 1, max = 20)
    private String email;

    @NotNull
    @Size(min = 1, max = 20)
    private String password;

    @Override
    public String toString() {
        return "User{name='" + username + "}";
    }
}
