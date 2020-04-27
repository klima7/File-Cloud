package project.server.backend;

/**
 * Interfejs umożliwia współpracę backendu z frontendem. Frontend może zdefiniować działania, jakie mogą być
 * wykonywane w przypadku zajścia poniższych zdarzeń. Może on chcieć na przykład odświeżyć listę wyświetlanych plików w
 * przypadku zmian w katalogu lub zaktualizować listę zalogowanych użytkownikóœ gdy nastąpi wylogowanie lub zalogowanie.
 * Frontend przekaże backendowi obiekt implementujący ten interfejs, a backend będzie wywoływał metody w odpowiednich momentach.
 */
public interface ServerListener {

    /**
     * Metoda definiująca reakcję na zalogowanie użytkownika.
     * @param username Login użytkownika.
     * @param directoryPath Ścieżka do katalogu użytkownika.
     */
    void userLoggedIn(String username, String directoryPath);

    /**
     * Metoda definiująca reakcję na wylogowanie użytkownika.
     * @param username Login użytkownika.
     */
    void userLoggedOut(String username);

    /**
     * Metoda definiująca reakcję na dokonanie zmian w katalogu użytkownika.
     * @param username Nazwa użytkownika w którego katalogu dokonano zmian.
     */
    void filesUpdated(String username);

    /**
     * Metoda definiująca reakcje na wygenerowanie przez backend raportu (logu).
     * @param message Treść raportu.
     */
    void log(String message);

    /**
     * Metoda definiująca reakcje na wystąpienie błędu krytycznego.
     * @param message Treść wiadomości o błędzie.
     */
    void errorOccured(String message);

}
