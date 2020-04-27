package project.server.backend;

import java.util.*;
import java.net.*;

/**
 * Klasa menedżera klientów odpowiedzialna za zarządzanie wszystkimi klientami, które są połączone z serwerem.
 * Gdy za pomocą menedżera dodawany jest nowy klient na słuchaczu zdarzeń wywoływana jest metoda userLoggedIn,
 * a gdy klient jest usuwany wywoływane jest metoda userLoggedOut.
 */
public class ServerClientsManager {

    // Parametry przekazane w konstruktorze
    private String rootDirectory;
    private int port;
    private ServerListener serverListener;

    // Mapowanie adresów klientów na objekty klientów
    private Map<InetAddress, ServerClient> clients = new HashMap<>();

    // Mapownie loginów użytkowników na objekty użytkowników
    private Map<String, ServerUser> users = new HashMap<>();

    /**
     * Konstruuje objekt menedżera klientów
     * @param rootDirectory Ścieżka do katalogu głównego serwera
     * @param port Numer portu na którym działa serwer i klienty
     * @param serverListener Objekt słuchacza zdarzeń
     */
    public ServerClientsManager(String rootDirectory, int port, ServerListener serverListener) {
        // Zapamiętanie przekazanych parametrów
        this.rootDirectory = rootDirectory;
        this.port = port;
        this.serverListener = serverListener;
    }

    /**
     * Metoda dodaje nowego klienta o przekazanym adresie IP oraz loginie.
     * @param address Adres IP nowego użytkownika.
     * @param login Login nowego użytkownika.
     */
    public void addClient(InetAddress address, String login) {
        ServerUser user = users.get(login);

        // Jeżeli nie ma zalogowanego żadnego innego klienta z tym loginem, to stwórz obiekt użytkownika
        if(user==null) {
            sendUserActiveEveryone(login);
            user = new ServerUser(login, rootDirectory, serverListener);
            users.put(login, user);

            // Wywołanie metod słuchacza
            serverListener.userLoggedIn(user.getLogin(), user.getDirectory());
            serverListener.log("# User " + login + " joined");
        }

        // Dodanie klienta do użytkownika
        ServerClient newClient = new ServerClient(address, user, port, serverListener);
        clients.put(address, newClient);
        user.registerClient(newClient);

        // Wysłanie wszystkim użytkownikom informacji o nowym aktywnym użytkowniku
        sendAllActiveUsersToClient(newClient);
    }

    /**
     * Metoda usuwa klienta
     * @param address Adres IP klienta, który będzie usunięty.
     */
    public void removeClient(InetAddress address) {
        // Usówanie klienta o podanym IP
        ServerClient client = clients.remove(address);
        client.logout();
        ServerUser user = client.getUser();
        user.unregisterClient(client);

        // Jeżeli żaden klient nie jest już zalogowany z tą nazwą użytkownika, to usuń też obiekt użytkownika
        if(user.getClientCount()==0) {
            users.remove(user);
            sendUserInactiveEveryone(user.getLogin());

            // Wywołanie metod słuchacza
            serverListener.userLoggedOut(user.getLogin());
            serverListener.log("# User " + user.getLogin() + " left");
        }
    }

    /**
     * Metoda zwraca obiekt klienta o podanym adresie IP. Klient ten musiał być wcześniej dodany do menedżera.
     * W przeciwnym wypadku metoda zwraca null.
     * @param address Adres ip klienta.
     * @return Obiekt klienta lub null.
     */
    public ServerClient getClient(InetAddress address) {
        return clients.get(address);
    }

    /**
     * Metoda zwraca obiekt użytkownika o podanym loginie. Jeżeli klient z podanym loginem nie został wcześniej
     * dodany do menedżera to metoda zwraca null.
     * @param login Login żądanego użytkownika.
     * @return Objekt żądanego użytkownika.
     */
    public ServerUser getUser(String login) {
        return users.get(login);
    }

    /**
     * Metoda wysyła wszystkim użytkownikom powiadomienie o nowym aktywnym
     * użytkowniku.
     * @param login Login nowego aktywnego użytkownika.
     */
    public void sendUserActiveEveryone(String login) {
        for(ServerUser user : users.values())
            user.sendActiveUserEveryone(login);
    }

    /**
     * Metoda wysyła wszystkim użytkownikom powiadomienie o nieaktywnym użytkowniku.
     * @param login Login nieaktywnego użytkownika.
     */
    public void sendUserInactiveEveryone(String login) {
        for(ServerUser user : users.values())
            user.sendInactiveUserEveryone(login);
    }

    /**
     * Metoda wysyła do wyznaczonego klienta ciąg wiadomości o wszystkich aktywnych użytkownikach.
     * @param client Klient do którego mają zostać wysłane powiadomienia o aktywnych użytkownikach.
     */
    public void sendAllActiveUsersToClient(ServerClient client) {
        for(ServerUser user : users.values())
            client.sendUserActive(user.getLogin());
    }

    /**
     * Metoda wysyła wszystkim użytkownikom powiadomienie, że serwer jest wyłączany.
     */
    public void sendServerDownEveryone() {
        for(ServerUser user : users.values())
            user.sendServerDownEveryone();
    }
}
