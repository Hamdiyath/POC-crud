package com.thanksye.poc_crud.dto;
import com.thanksye.poc_crud.entity.TaskStatus;
import java.time.LocalDateTime;
public record TaskResponse(

        Long id,
        String title,
        String description,
        TaskStatus  status ,
        LocalDateTime createdAt


) {
}
