package project.client.frontend;

import javafx.application.*;
import project.client.backend.*;
import java.util.concurrent.*;

/**
 * Klasa definiuje działania, jakie mają być wykonywane w odpowiedzi na zdarzenia zachądzące po stronie backendu.
 */
public class ClientHandler implements ClientListener {

    public ClientLayoutController controller;

    /**
     * Konstruktor wymagający kontrolera interfejsu, którego metody są wywoływane w odpowiedzi na zachodzące zdarzenia.
     * @param controller Kontroler interfejsu klienta.
     */
    public ClientHandler(ClientLayoutController controller) {
        this.controller = controller;
    }

    /**
     * Metoda aktualizuję listę wyświetlanych plików w odpowiedzi na zmianę plików w katalogu lokalnym.
     */
    public void filesUpdated() {
        Platform.runLater(() -> controller.updateFilesList());
    }

    /**
     * Metoda wyświetla logi w panelu z logami w odpowiedzi na ich generowanie przez serwer.
     * @param message Treść wiadomości.
     */
    public void log(String message) {
        Platform.runLater(() -> controller.addLog(message));
    }

    /**
     * Metoda dodaję nową kartę w interfejsie użytkownika w odpowiedzi na zalogowanie się nowego użytkownika.
     * @param login Login nowo zalogowanego użytkownika.
     */
    public void userLogginIn(String login) {
        Platform.runLater(() -> controller.addUser(login));
    }

    /**
     * Metoda usówa kartę w interfejsie użytkownika w odpowiedzi na wylogowanie się użytkownika.
     * @param login Login użytwkoniak, który się wylogował.
     */
    public void userLogginOut(String login) {
        Platform.runLater(() -> controller.removeUser(login));
    }

    /**
     * Metoda wyświętla komunikat o błędzie, odczekuje 5 sekund i kończy działanie aplikacji w odpowiedzi na błąd krytyczny.
     */
    public void errorOccured() {
        Platform.runLater(() -> {
            controller.addLog("!! Connection failure, server is probably down. Quiting in 5 seconds");
        });

        try { TimeUnit.SECONDS.sleep(5); }
        // Oczekiwanie zostanie przerwane, gdy klient zamknie aplikacje sam przed upływem 5 sekund, co może oczywiście zrobić.
        catch(InterruptedException e) {}
        System.exit(0);
    }
}
