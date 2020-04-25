package project.server.frontend;

import project.server.backend.*;
import javafx.application.*;

/**
 * Klasa definiuje zachowanie aplikacji dla wszystkich możliwych zdarzeń, które może wygenerować backend.
 * W odpowiedzi na zdarzenia klasa wykonuje pewne operacje na interfejsie użytkownika, do którego ma dostęp
 * poprzez kontroler przekazany w konstruktorze.
 */
public class ServerHandler implements ServerListener {

    private ServerLayoutController controller;

    /**
     * Konstruuje objekt operujący na danym kontrolerze.
     * @param controller Kontroler interfejsu użytkownika.
     */
    public ServerHandler(ServerLayoutController controller) {
        this.controller = controller;
    }

    /**
     * Metoda powoduje dodanie nowej karty do interfejsu odpowiadającej nowo zalogowanemu użytkownikowi.
     * @param login Login użytkownika który dołączył.
     * @param directoryPath Ścieżka do katalogu użytkownika który dołączył.
     */
    public void userLoggedIn(String login, String directoryPath) {
        Platform.runLater(() -> controller.addTab(login, directoryPath));
    }

    /**
     * Metoda powoduje usunięcie karty odpowiadającej danemu użytkownikowi z interfejsu serwera
     * @param login Login użytkownika, który opuścił serwer.
     */
    public void userLoggedOut(String login) {
        Platform.runLater(() -> controller.removeTab(login));
    }

    /**
     * Metoda powoduje odświeżenie listy plików danego użytkownika wyświetlane w jego karcie.
     * @param username Login użytkownika, którego katalog został zmodyfikowany.
     */
    public void filesUpdated(String username) {
        Platform.runLater(() -> controller.updateTab(username));
    }

    /**
     * Metoda powoduje wyświetlenie wiadomości w dolnym panelu z logami.
     * @param message Treść wiadomości.
     */
    public void log(String message) {
        Platform.runLater(() -> controller.addLog(message));
    }

    /**
     * Metoda powoduje wyświetlenie komunikatu o błędzie krytycznym i zamknięcie aplikacji serwera.
     * @param message Treść wiadomości.
     */
    public void errorOccured(String message) {
        //TODO Uzupełnić zawartość
    }
}
