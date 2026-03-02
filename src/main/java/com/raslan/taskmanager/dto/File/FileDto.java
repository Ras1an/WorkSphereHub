package com.raslan.taskmanager.dto.File;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto {
    private Long id;
    private String name;
}
