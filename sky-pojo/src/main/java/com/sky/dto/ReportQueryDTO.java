package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ReportQueryDTO implements Serializable {
    private Integer status;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
