package com.example.form_flow_backend.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DummyData {
    @Id
    @NotNull
    private int id;

    @NotNull
    @Size(min = 1, max = 20)
    private String name;

    @NotNull
    @Min(1)
    private int age;

    @Override
    public String toString() {
        return "MyData{name='" + name + "', age=" + age + "}";
    }
}
