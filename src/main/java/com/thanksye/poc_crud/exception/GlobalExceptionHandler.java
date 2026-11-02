package com.thanksye.poc_crud.exception;

import com.thanksye.poc_crud.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice : controller spécial sans route. Quand une exception remonte d'un
// controller ou d'un service, Spring cherche ici la méthode qui la prend en charge.
// Il choisit toujours le handler le plus spécifique.
// @Slf4j (Lombok) : fournit un objet "log" pour écrire dans la console.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 : tâche introuvable (levée par TaskService).
    // Le message vient de la phrase écrite dans le service : on peut l'exposer sans risque.
    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleTaskNotFound(TaskNotFoundException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    // 400 : une contrainte de @Valid est violée (titre vide, titre trop long...).
    // On renvoie une table champ -> message, avec les messages écrits dans TaskRequest.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ApiResponse.error("Données invalides", errors);
    }

    // 400 : JSON mal formé, ou valeur d'enum inconnue (ex. "status": "DOEN").
    // Message fixe : celui de l'exception contient des détails techniques de Jackson.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleUnreadable(HttpMessageNotReadableException ex) {
        return ApiResponse.error("Corps de la requête invalide ou illisible");
    }

    // 400 : paramètre d'URL du mauvais type (ex. /tasks/abc au lieu d'un nombre).
    // ex.getName() renvoie le nom du paramètre ("id"), sans risque à exposer.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ApiResponse.error("Le paramètre '" + ex.getName() + "' a une valeur invalide");
    }

    // 404 : URL qui ne correspond à aucune route (ex. /tasks/ avec le slash final).
    // Sans ce handler, le handler général ci-dessous la transformerait en 500.
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoResource(NoResourceFoundException ex) {
        return ApiResponse.error("Ressource introuvable");
    }

    // 500 : filet de sécurité pour tout ce qui n'a pas de handler plus précis.
    // Message fixe : ex.getMessage() peut contenir du SQL, des chemins ou des noms de classes.
    // La cause réelle est écrite dans les logs, visibles uniquement côté serveur.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnexpected(Exception ex) {
        log.error("Erreur inattendue", ex);
        return ApiResponse.error("Erreur interne du serveur");
    }
}