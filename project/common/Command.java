package project.common;

/**
 * Typ wyliczeniowy zawierający wszystkie komendy za pomocą których zachomi komunikacja klienta z serwerem.
 */
public enum Command {
    /** Komenda logowania, wysyłana przez klienty, występuje po niej login użytkownika. */
    LOGIN(1),
    /** Komenda wylogowania, wysyłana przez klienty, nic po niej nie występuje. */
    LOGOUT(2),
    /** Komenda udanego logowania, wysyłana przez serwer, nic po niej nie występuje. */
    LOGIN_SUCCESS(3),
    /** Komenda wysyłania pliku, wysyłana przez serwer i klienty, występuje po niej nazwa pliku, data modyfikacji i dane pliku. */
     SEND_FILE(4),
    /** Komenda żądania usunięcia pliku, wysyłana przez serwer i klienty, występuje po niej nazwa pliku. */
    DELETE_FILE(5),
    /** Komenda informująca o istnieniu pliku, wysyłana przez serwer i klienty, występuje po niej nazwa pliku, rozmiar i data modyfikacji. */
    CHECK_FILE(6),
    /** Komenda żądania przesłania pliku przez drugą stronę, wysyłana przez serwer i klienty, występuje po niej nazwa pliku. */
    NEED_FILE(7),
    /** Komenda informująca o aktywnym użytkowniku, wysyłana przez serwer, występuje po niej nazwa użytkownika */
    USER_ACTIVE(8),
    /** Komenda informująca o nieaktywnym użytkowniku, wysyłana przez serwer, występuje po niej nazwa użytkownika */
    USER_INACTIVE(9),
    /** Komenda wysyłania pliku przez jednego klienta do drugiego, wysyłana jedynie przez klienty, występuje po niej nazwa pliku,
     * rozmiar, data modyfikacji oraz zawartość pliku.*/
    SEND_TO_USER(10),
    /** Komenda informująca o zatrzymywaniu serwera, wysyłana przez serwer, nic po niej nie występuje */
    SERVER_DOWN(11);

    // Liczba jednoznacznie identyfikująca komendę
    private int number;

    /**
     * Konstruktor tworzy komendę o przekazanym identyfikatorze.
     * @param number
     */
    Command(int number) {
        this.number = number;
    }

    /**
     * Metoda zwraca identyfikator komendy.
     * @return Liczba jednoznacznie identyfikująca komendę
     */
    public int asInt() {
        return this.number;
    }
}
