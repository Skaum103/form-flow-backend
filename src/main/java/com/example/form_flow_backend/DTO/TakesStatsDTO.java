package com.example.form_flow_backend.DTO;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.secretsmanager.endpoints.internal.Value;

import java.util.HashMap;

@Getter
@Setter
public class TakesStatsDTO {
    private Integer question_order;
    private HashMap<String, Integer> stats;
}
