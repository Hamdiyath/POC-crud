package com.thanksye.poc_crud.dto;
import com.thanksye.poc_crud.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record TaskRequest(

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 150, message = "Le titre ne doit pas depasser 150")
        String title,

        @Size(max = 1000 , message = "La description ne doit pas depasser 1000 caracteres")
        String description,

        TaskStatus status


) {


}
