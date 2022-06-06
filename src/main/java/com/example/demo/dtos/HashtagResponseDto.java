package com.example.demo.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
@Data
public class HashtagResponseDto {

    private String label;

    private Timestamp firstUsed;

    private Timestamp lastUsed;

}
