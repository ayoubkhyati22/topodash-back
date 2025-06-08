package com.topographe.topographe.dto.request;

import lombok.Data;
import java.util.Set;

@Data
public class TaskAssignRequest {
    private Set<Long> technicienIds;
    private String assignmentNote;
    private boolean replaceExisting = false;
}