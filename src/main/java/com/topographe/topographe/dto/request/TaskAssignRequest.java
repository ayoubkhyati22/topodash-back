package com.topographe.topographe.dto.request;

import lombok.Data;

@Data
public class TaskAssignRequest {

    private Long technicienId;
    private String assignmentNote;
}