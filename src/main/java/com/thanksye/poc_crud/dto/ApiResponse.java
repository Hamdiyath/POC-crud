package com.thanksye.poc_crud.dto;

// Enveloppe commune à toutes les réponses de l'API.
// <T> : type du contenu, choisi à l'utilisation (ApiResponse<TaskResponse>, ApiResponse<List<...>>...).
// Les trois champs sont déclarés dans la parenthèse : Java génère le constructeur
// et les accesseurs success(), message() et data(), que Jackson utilise pour produire le JSON.
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {

    // Fabrique une réponse de succès : success = true, avec un message et le contenu.
    // static : s'appelle directement sur le record (ApiResponse.success(...)), sans créer d'objet.
    // Le <T> devant le type de retour déclare le type générique propre à cette méthode.
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // Fabrique une réponse d'erreur : success = false, un message, et aucun contenu (null).
    // Utilisée plus tard par le gestionnaire d'exceptions.
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
    // Fabrique une réponse d'erreur avec un contenu (ex. la liste des champs invalides).
// Même nom que la méthode précédente mais paramètres différents : c'est la surcharge,
// Java choisit la bonne version selon les arguments passés.
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}