package project.server.backend;

import java.io.*;
import java.net.*;
import project.common.*;

/**
 * Klasa umożliwia uruchomienie serwera z daną konfiguracją oraz jego zatrzymanie. Po stworzeniu objektu
 * serwer jeszcze nie jest uruchomiony, w tym celu należy użyć metody startServer. Aby go nastęþnie zatrzymać używa się
 * metody stopServer, lecz kolejne jego uruchomienie jest wówczas niemożliwe i objekt staje się już nieprzydatny.
 *
 * Podczas konstrukcji objektu należy przekazać jako parametr objekt implementujący interfejs ServerListener.
 * Jest to interfejs, który umożliwia użytkowniką klasy zareagowanie na pewne zdarzenia, które mogą wystąpić
 * po stronie serwera.
 */
public class ServerBackend {

    /**
     * Objekt reprezentujący możliwe stany serwera.
     */
    public enum State {
        /** Serwer jest gotowy do uruchomienia. */
        READY,
        /** Serwer jest uruchomiony. */
        RUNNING,
        /** Serwer jest zatrzymany. */
        SHUT_DOWN;
    }

    // Parametry, które zostały przekazane w konstruktorze
    private int port;
    private String rootDirectory;
    private ServerListener serverListener;

    // Soket na którym serwer oczekuja na połączenia
    private ServerSocket serverSocket;

    // Wątek, który akceptuje wszystkie przychodzące połączenia
    private Thread acceptingThread;

    // Objekt zarządzający wszystkimi aktywnymi klientami
    private ServerClientsManager clientsManager;

    // Aktualny stan serwera
    State state;

    /**
     * Konstruuje obiekt serwera o podanej konfiguracji.
     * @param rootDirectory Ścieżka do bazowego katalogu serwera, w którym będą umieszczane katalogi poszczególnych
     *                      użytkoników. Jeżeli katalog nie istnieje to zostanie utworzony.
     * @param port Numer portu na którym będzie nasłuchiwał serwer i na który będzie wysyłał dane klientom.
     * @param serverListener Interfejs umożliwiający zdefiniowanie reakcji na zdarzenia, które mogą wystąpić po stronie serwera.
     */
    public ServerBackend(String rootDirectory, int port, ServerListener serverListener) {
        // Zapamiętami przekazanym parametrów
        this.port = port;
        this.rootDirectory = rootDirectory;
        this.serverListener = serverListener;

        // Ustawienie stanu serwera
        this.state = State.READY;

        // Stworzenie menadżera klientów
        this.clientsManager = new ServerClientsManager(rootDirectory, port, serverListener);

        // Stworzenie katalogu serwera jeżeli nie istnieje
        File directory = new File(rootDirectory);
        if(!directory.exists())
            directory.mkdirs();
    }

    /**
     * Metoda zwraca numer port na którym działa serwer.
     * @return numer portu.
     */
    public int getPort() {
        return port;
    }

    /**
     * Metoda zwraca ścieżkę do katalogu serwera, zawierająca katalogi poszczególnych użytkowników.
     * @return Ścieżka do katalogu serwera.
     */
    public String getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Metoda zwraca objekt słuchacza, który został przekazany w trakcie konstrukcji objektu.
     * @return Objekt słuchacza.
     */
    public ServerListener getServerListener() {
        return serverListener;
    }

    /**
     * Metoda zwraca aktualny stan serwera.
     * @return Aktualny stan serwera.
     */
    public State getState() {
        return state;
    }

    /**
     * Metoda uruchamia działanie serwera. Uruchamia potrzebne funkcję w osobnych wątkach i natychmiast kończy działanie.
     * @throws IOException
     */
    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port, 256, InetAddress.getByName(Constants.SERVER_ADDRESS));
        acceptingThread = new Thread(new ServerAccepter(serverSocket, clientsManager, serverListener));
        acceptingThread.start();
        serverListener.log("# Server running on port " + port);
    }

    public void stopServer() throws IOException {
        clientsManager.sendServerDownEveryone();
        acceptingThread.interrupt();
        serverSocket.close();
    }
}
