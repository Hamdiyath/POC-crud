package com.thanksye.poc_crud.controller;

import com.thanksye.poc_crud.dto.ApiResponse;
import com.thanksye.poc_crud.dto.TaskRequest;
import com.thanksye.poc_crud.dto.TaskResponse;
import com.thanksye.poc_crud.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController : Spring crée ce bean, expose ses méthodes comme endpoints HTTP
// et convertit automatiquement les objets renvoyés en JSON.
// @RequestMapping : préfixe commun à toutes les routes (équivalent du prefix d'un APIRouter).
@RestController
@RequestMapping("/tasks")
public class TaskController {

    // Le controller ne connaît que le service, jamais le repository.
    private final TaskService taskService;

    // Injection par constructeur : Spring fournit le service automatiquement.
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // POST /tasks : crée une tâche.
    // @RequestBody : Spring convertit le JSON reçu en TaskRequest (rôle de Pydantic).
    // @Valid : déclenche les contraintes du DTO (@NotBlank, @Size). Sans lui, elles sont ignorées.
    // @ResponseStatus : renvoie 201 Created au lieu du 200 par défaut.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> createTask(@RequestBody @Valid TaskRequest taskRequest) {
        return ApiResponse.success("Tâche créée" , taskService.create(taskRequest));
    }

    // GET /tasks : liste toutes les tâches.
    @GetMapping
    public ApiResponse<List<TaskResponse>> findAllTasks() {
        return ApiResponse.success("Liste des tâches récupérée", taskService.findAll());
    }

    // GET /tasks/{id} : renvoie une tâche.
    // @PathVariable : récupère la valeur {id} de l'URL (équivalent de task_id: int dans FastAPI).
    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> findTaskById(@PathVariable Long id) {
        return ApiResponse.success("Tâche récupérée", taskService.findById(id));
    }

    // PUT /tasks/{id} : modifie une tâche.
    @PutMapping("/{id}")
    public ApiResponse<TaskResponse>  updateTask(@PathVariable Long id, @RequestBody @Valid TaskRequest taskRequest) {
        return ApiResponse.success("Tâche modifiée",taskService.update(id, taskRequest));
    }

    // DELETE /tasks/{id} : supprime une tâche.
// 200 OK au lieu de 204 : une réponse qui a un corps (l'enveloppe) ne peut pas être un 204.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteTaskById(@PathVariable Long id) {
        // Le service ne renvoie rien : on l'appelle sur sa propre ligne.
        taskService.delete(id);

        // Pas de contenu à renvoyer : data vaut null.
        return ApiResponse.success("Tâche supprimée", null);
    }
}