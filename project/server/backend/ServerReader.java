package project.server.backend;

import project.common.*;
import java.io.*;
import java.net.*;

/**
 * Zadanie odpowiedzialne za odczytywanie danych z socketa, interpretowanie odczytanych danych jako komend i
 * odpowiednią reakcję na nie.
 */
public class ServerReader implements Runnable {

    // Parametry przekazane w konstruktorze
    private Socket socket;
    private ServerClientsManager clientsManager;
    private ServerListener serverListener;

    /**
     * Konstruuje obiekt ServerReader, który odczyta rozkaz z przekazanego socketa, w razie potrzeby doda lub usunię
     * użytkownika korzystając z przekazanego clientsManagera oraz będzie zgłaszała zachodzące zdarzenia za pomocą
     * serverListenera.
     * @param socket Socket z którego będzie odczytany rozkaz.
     * @param clientsManager Menedżer za pomocą którego będą dodawane oraz usówane klienty.
     * @param serverListener Słuchacz za pomocą którego będą zgłaszanie zachodzące zdarzenia.
     */
    public ServerReader(Socket socket, ServerClientsManager clientsManager, ServerListener serverListener) {
        this.socket = socket;
        this.clientsManager = clientsManager;
        this.serverListener = serverListener;
    }

    /**
     * Metoda zawiera działanie zadania.
     */
    public void run() {
        try {
            // Odczytanie adresu IP oraz komendy
            InetAddress address = socket.getInetAddress();
            DataInputStream input = new DataInputStream(socket.getInputStream());
            int command = input.readInt();

            // Otrzymano komendę logowania
            if(command==Command.LOGIN.asInt()) {
                String login = input.readUTF();
                clientsManager.addClient(address, login);
                serverListener.log(">> receiving login request from " + address.getHostName() + "(" + login + ")" + " to " + login);
                ServerClient client = clientsManager.getClient(address);
                client.sendLoginSuccess();
                client.sendAdvertisements();
            }

            // Otrzymano komendę wylogowania
            else if(command==Command.LOGOUT.asInt()) {
                serverListener.log(">> receiving logout from " + clientsManager.getClient(address));
                clientsManager.removeClient(address);
            }

            // Użytkownik przesłał plik
            else if(command==Command.SEND_FILE.asInt()) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();
                ServerClient client = clientsManager.getClient(address);
                ServerUser user = client.getUser();
                serverListener.log(">> receiving file " + relativePath + " from " + client);
                user.receiveFileData(relativePath, modificationTime, size, input);
                user.sendFileExcept(relativePath, client);
            }

            // Użytkownik wysłał plik do innego użytkownika
            else if(command==Command.SEND_TO_USER.asInt()) {
                String login = input.readUTF();
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();

                ServerClient sendingClient = clientsManager.getClient(address);
                ServerUser user = clientsManager.getUser(login);
                serverListener.log(">> receiving file " + relativePath + " from " + sendingClient + " to " + login);

                // Jeżeli użytkownik docelowy istnieje
                if(user != null) {
                    relativePath += " (from " + sendingClient.getUser().getLogin() + ")";
                    user.receiveFileData(relativePath, modificationTime, size, input);
                    user.sendFileEveryone(relativePath);
                }
            }

            // Otrzymano żądanie wysłania pliku
            else if(command==Command.NEED_FILE.asInt()) {
                ServerClient client = clientsManager.getClient(address);
                String relativePath = input.readUTF();
                serverListener.log(">> receiving file request for " + relativePath + " from " + client);
                client.sendFile(relativePath);
            }

            // Otrzymano żądanie usunięcia pliku
            else if(command==Command.DELETE_FILE.asInt()) {
                String relativePath = input.readUTF();
                ServerClient client = clientsManager.getClient(address);
                serverListener.log(">> receiving delete request for " + relativePath + " from " + client);
                ServerUser user = client.getUser();
                boolean deleted = user.deleteFile(relativePath);
                if(deleted) user.sendDeleteExcept(relativePath, client);
            }

            // Otrzymano ogłoszenie o aktualnym pliku
            else if(command==Command.CHECK_FILE.asInt()) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();

                ServerClient client = clientsManager.getClient(address);
                serverListener.log(">> Checking if file " + relativePath + " from " + client + " is up to date");
                if(!client.getUser().isUpToDate(relativePath, modificationTime))
                    client.sendRequest(relativePath);
            }

        } catch(IOException e) {
            serverListener.log("!! Error occured while receiving message");
        }
    }
}