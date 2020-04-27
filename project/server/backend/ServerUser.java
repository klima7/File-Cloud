package project.server.backend;

import java.io.*;
import java.util.*;

/**
 * Klasa reprezentuje pojedyńczego użytkownika. Użytkonik posiada własny login oraz katalog.
 * Na danego użytkownika może składać się dowolnie wiele klientów, które zalogowały się za pomocą tego samego loginu.
 */
public class ServerUser {

    // Parametry podawane w konstruktorze
    private String login;
    private String directory;
    private ServerListener serverListener;

    // Lista klientów związanych z danym użytkownikiem
    private List<ServerClient> clients = new LinkedList();

    /**
     * Konstruuje obiekt użytkownika o podanym loginie oraz katalogu.
     * @param login Login użytkownika.
     * @param rootDirectory Katalog użytkownika.
     * @param serverListener Obiekt słuchacza.
     */
    public ServerUser(String login, String rootDirectory, ServerListener serverListener) {
        // Zapamiętanie parametrów
        this.login = login;
        this.directory = rootDirectory + "/" + login;
        this.serverListener = serverListener;

        // Stworzenie katalogu użytkownika, jeżeli nie istnieje
        File file = new File(directory);
        if(!file.exists())
            file.mkdir();
    }

    /**
     * Metoda zwraca ścieżkę do katalogu użytkownika.
     * @return Ścieżka do katalogu użytkownika.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Metoda zwraca login użytkownika.
     * @return Login użytkownika.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Metoda zwraca obiekt słuchacza.
     * @return obiekt słuchacza.
     */
    public ServerListener getServerListener() {
        return serverListener;
    }

    /**
     * Metoda dodaje do użytkownika podanego klienta.
     * @param client Dodawany klient.
     */
    public void registerClient(ServerClient client) {
        clients.add(client);
    }

    /**
     * Metoda usuwa z użytkownika podanego klienta.
     * @param client Usuwany klient.
     */
    public void unregisterClient(ServerClient client) {
        clients.remove(client);
    }

    /**
     * Metoda zwraca liczbę klientów związanych z użytkownikiem.
     * @return Liczba klientów związanych z użytkownikeim.
     */
    public int getClientCount() {
        return clients.size();
    }

    /**
     * Metoda usuwa plik w katalogu użytkownika i wywołuje na słuchaczu metodę filesUpdated, ponieważ katalog użytkownika się zmienił.
     * @param relativePath Ścieżka do usówanego pliku względem katalogu użytkownika.
     * @return true jeżeli plik został usunięty, false jeżeli plik już wcześniej nie istniał.
     */
    public boolean deleteFile(String relativePath) {
        // Usunięcie pliku
        File file = new File(directory, relativePath);
        boolean deleted = file.delete();

        // Powiadomienie o dokonaniu zmian w katalogu serwera jeżeli nastąpiły
        if(deleted)
            serverListener.filesUpdated(login);

        return deleted;

    }

    /**
     * Metoda zwraca odpowiedź czy plik dany ścieżką jest aktualny poprzez porównuje jego czasu modyfikacji z czasem modyfikacji podanym jako parametr.
     * @param relativePath Ścieżka do pliku, którego aktualność sprawdzamy, względem katalogu użytkownika.
     * @param compareTime Czas modyfikacji z którym czas modyfikacji pliku jest porównywany.
     * @return true jeżeli plik jest aktualny, false jeżeli nie jest aktualny.
     */
    public boolean isUpToDate(String relativePath, long compareTime) {
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();
        if(modificationTime < compareTime)
            return false;
        return true;
    }

    /**
     * Metoda wysyła dany plik wszystkim klientom należącym do użytkownika oprócz jednego, np. klienta od którego dany plik mógł został otrzymany.
     * @param relativePath Ścieżka do wysyłanego pliku względem katalogu użytkownika.
     * @param except Klient do którego plik ma nie być wysłany.
     */
    public void sendFileExcept(String relativePath, ServerClient except) {
        for(ServerClient client : clients) {
            if(client != except)
                client.sendFile(relativePath);
        }
    }

    /**
     * Metoda wysyła dany plik wszystkim klientom należącym do użytkownika.
     * @param relativePath Ścieżka do wysyłanego pliku względem katalogu użytkownika.
     */
    public void sendFileEveryone(String relativePath) {
        for(ServerClient client : clients)
            client.sendFile(relativePath);
    }

    /**
     * Metoda wysyła do wszystkich klientów należący do użytkownika, oprócz jednego, żądanie usunięcia pliku.
     * @param relativePath Ścieżka do pliku którego żądanie usnięcia ma być wysłane.
     * @param except Klient do którego żądanie usunięcia pliku ma nie być wysyłane.
     */
    public void sendDeleteExcept(String relativePath, ServerClient except) {
        for(ServerClient client : clients) {
            if(client!=except)
                client.sendDelete(relativePath);
        }
    }

    /**
     * Metoda wysyła żądanie usunięcia pliku do wszystkich klientów należących do użytkownika.
     * @param relativePath Ścieżka do pliku którego żądanie usnięcia ma być wysłane.
     */
    public void sendDeleteEveryone(String relativePath) {
        for(ServerClient client : clients)
            client.sendDelete(relativePath);
    }

    /**
     * Metoda wysyła wszystkim klientom należącym do użytkownika wiadomość o aktywnym użytkowniku.
     * @param login Login aktywnego użytkownika.
     */
    public void sendActiveUserEveryone(String login) {
        for(ServerClient client : clients)
            client.sendUserActive(login);
    }

    /**
     * Metoda wysyła wszystkim klientom należącym do użytkownika wiadomość o nieaktywnym użytkowniku.
     * @param login Login nieaktywnego użytkownika.
     */
    public void sendInactiveUserEveryone(String login) {
        for(ServerClient client : clients)
            client.sendUserInactive(login);
    }

    /**
     * Metoda wysyła wszystkim klientom należącym do użytkownika powiadomienie, że serwer kończy działanie.
     */
    public void sendServerDownEveryone() {
        for(ServerClient client : clients)
            client.sendServerDown();
    }

    /**
     * Metoda tworzy plik w katalogu użytkownika i wczytuje do niego zawartość ze strumienia.
     * @param relativePath Ścieżka do pliku który ma zostać utworzony względem katalogu użytkownika.
     * @param modificationTime Ostatnia data modyfikacji, która ma być ustawiona dla pliku.
     * @param size Ilość danych, która ma zostć wczytana ze strumienia.
     * @param input Śtrumień z którego mają zostać wczytane dane do pliku.
     */
    public void receiveFileData(String relativePath, long modificationTime, long size, DataInputStream input) {

        // Usuwanie pliku jeżeli taki już istnieje
        File newFile = new File(directory, relativePath);
        if (newFile.exists())
            newFile.delete();

        // Tworzenie nowego pliku
        try {
            newFile.createNewFile();
        } catch(IOException e) {
            serverListener.log("!! Unable to create file in user directory");
            return;
        }

        // Wpisywanie do pliku zawartości odczytywanej ze strumienia
        try(FileOutputStream output = new FileOutputStream(newFile)) {
            for (long pos = 0; pos < size; pos++) {
                int aByte = input.read();
                output.write(aByte);
            }
        } catch(IOException e) {
            serverListener.log("!! Error occured while receiving file " + relativePath);
        }

        // Ustawienie ostatniej daty modyfikacji pliku
        newFile.setLastModified(modificationTime);

        // Powiadomienie o dokonaniu zmian w katalogu użytkownika
        serverListener.filesUpdated(login);
    }
}
