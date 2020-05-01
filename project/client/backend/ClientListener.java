package project.client.backend;

/**
 * Interfejs umożliwia współpracę backendu klienta z frontendem. Frontend może zdefiniować działania, jakie mają być
 * wykonywane w przypadku zajścia poniższych zdarzeń. Może on chcieć na przykład odświeżyć listę wyświetlanych plików w
 * przypadku zmian w katalogu lub zaktualizować listę zalogowanych użytkowników, gdy nastąpi wylogowanie lub zalogowanie.
 */
public interface ClientListener {

    /**
     * Metoda definiująca reakcję na zajście zmian w katalogu użytkownika.
     */
    void filesUpdated();

    /**
     * Metoda definiująca reakcje na wygenerowanie przez backend logu.
     * @param message Treść raportu.
     */
    void log(String message);

    /**
     * Metoda definiująca reakcje na wystąpienie błędu krytycznego.
     */
    void errorOccured();

    /**
     * Metoda definiująca reakcję na zalogowanie nowego użytkownika.
     * @param login Login użytkownika.
     */
    void userLogginIn(String login);

    /**
     * Metoda definiująca reakcję na wylogowanie użytkownika.
     * @param login Login użytkownika.
     */
    void userLogginOut(String login);

}
