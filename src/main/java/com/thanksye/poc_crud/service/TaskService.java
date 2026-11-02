package com.thanksye.poc_crud.service;

import com.thanksye.poc_crud.dto.TaskRequest;
import com.thanksye.poc_crud.dto.TaskResponse;
import com.thanksye.poc_crud.entity.Task;
import com.thanksye.poc_crud.exception.TaskNotFoundException;
import com.thanksye.poc_crud.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service : Spring crée ce bean au démarrage et peut l'injecter dans le controller.
// Le service contient la logique métier : il est la seule couche qui connaît à la fois
// les DTOs (API) et les entités (base de données).
@Service
public class TaskService {

    // Dépendance assignée une seule fois dans le constructeur (final).
    // private : le controller ne doit jamais parler directement au repository.
    private final TaskRepository taskRepository;

    // Injection par constructeur : Spring fournit le repository automatiquement.
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Méthodes publiques : appelées par le controller

    // Crée une nouvelle tâche à partir des données reçues du client.
    // @Transactional : Spring ouvre la session avant la méthode et valide l'écriture à la fin
    // (équivalent de commit() ; rollback automatique en cas d'exception).
    @Transactional
    public TaskResponse create(TaskRequest request) {
        // Entité vide : son statut vaut déjà TODO grâce à la valeur par défaut de Task.
        Task task = new Task();

        // Copie titre, description et statut (si fourni) de la requête vers l'entité.
        applyRequest(request, task);

        // save() insère la ligne et renvoie l'entité enregistrée,
        // avec l'id généré par PostgreSQL et createdAt rempli par @PrePersist.
        Task saved = taskRepository.save(task);

        // On convertit l'entité enregistrée (pas "task") en DTO de réponse.
        return toResponse(saved);
    }

    // Renvoie toutes les tâches.
    // readOnly = true : aucune modification, Spring peut optimiser la transaction.
    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        // findAll() renvoie une List<Task> ; stream().map(...) convertit chaque élément
        // (équivalent de [to_response(t) for t in tasks] en Python).
        // this::toResponse = référence à la méthode toResponse de cette classe.
        return taskRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Renvoie une tâche par son id (TaskNotFoundException si elle n'existe pas).
    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return toResponse(getTaskOrThrow(id));
    }

    // Modifie une tâche existante.
    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        // Récupère la tâche existante (ou lève l'exception si l'id n'existe pas).
        Task task = getTaskOrThrow(id);

        // Copie les nouvelles valeurs dans l'entité.
        applyRequest(request, task);

        // Pas de save() : la tâche est gérée par la transaction, Hibernate détecte
        // les champs modifiés et écrit le UPDATE à la fin de la méthode.
        return toResponse(task);
    }

    // Supprime une tâche.
    @Transactional
    public void delete(Long id) {
        // Vérifie que la tâche existe : sinon le client reçoit un 404 clair.
        Task task = getTaskOrThrow(id);

        // Supprime la ligne correspondante en base.
        taskRepository.delete(task);
    }

    // Méthodes privées : utilisées uniquement à l'intérieur de ce service

    // Retrouve une tâche par son id, ou lève TaskNotFoundException si elle n'existe pas.
    // Partagée par findById, update et delete : le message d'erreur reste identique.
    private Task getTaskOrThrow(Long id) {
        // findById renvoie un Optional<Task> (une boîte qui contient la tâche ou rien).
        // orElseThrow : renvoie la tâche si elle est présente, sinon lève l'exception fournie.
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Tâche introuvable avec l'id " + id));
    }

    // Convertit une entité Task (lue en base) en TaskResponse (renvoyée au client).
    private TaskResponse toResponse(Task task) {
        // L'ordre doit être celui des champs du record TaskResponse :
        // id, title, description, status, createdAt.
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt()
        );
    }

    // Copie les données d'une requête (TaskRequest) dans une entité Task existante.
    // void : la méthode ne renvoie rien, elle modifie directement l'objet "task" reçu.
    private void applyRequest(TaskRequest request, Task task) {
        // request.title() : un record n'a pas de préfixe "get" sur ses accesseurs.
        task.setTitle(request.title());
        task.setDescription(request.description());

        // Le statut est facultatif : on ne le copie que s'il est fourni.
        // Sinon on garde la valeur actuelle (TODO par défaut à la création),
        // ce qui évite d'écrire null dans une colonne not null.
        if (request.status() != null) {
            task.setStatus(request.status());
        }
    }
}