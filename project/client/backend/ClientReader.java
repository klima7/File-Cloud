package project.client.backend;

import project.common.Command;

import java.io.*;
import java.net.*;

/**
 * Zadanie odpowiedzialne za odczytanie danych ze strumienia wejściowego socketa, zinterpretowanie ich jako rozkazu
 * oraz podjęcie odpowiedniego działania.
 */
class ClientReader implements Runnable {

    // Parametry przekazane w konstruktorze
    private Socket socket;
    private ClientBackend clientBackend;
    private ClientListener clientListener;

    /**
     * Konnstruuje obiekt odczytujący dane z przekazanego socketa, posługując się przy tym przekazanym backendem.
     * @param socket Socket z którego będzie odczytany rozkaz.
     * @param clientBackend Backend który będzie wykorzystywany przy reagowaniu na otrzymane polecenia.
     * @param clientListener Obiekt słuchacza używany do powiadamiania frontendu o zachodzących zdarzeniach.
     */
    public ClientReader(Socket socket, ClientBackend clientBackend, ClientListener clientListener) {
        this.socket = socket;
        this.clientBackend = clientBackend;
        this.clientListener = clientListener;
    }

    /**
     * Zadanie odczytujące rozkaz ze strumienia i odpowiednio reagujące.
     */
    public void run() {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            int command = input.readInt();

            // Udało się pomyślnie zalogować
            if (command == Command.LOGIN_SUCCESS.asInt()) {
                clientListener.log(">> receiving login success");
                clientBackend.sendFileCheckAll();
            }

            // Otrzymano wiadomość o istnieniu pliku na serwerze
            else if (command == Command.CHECK_FILE.asInt()) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                clientListener.log(">> receiving advertisement for file " + relativePath);
                if(!clientBackend.isFileUpToDate(relativePath, modificationTime))
                    clientBackend.sendFileRequest(relativePath);
            }

            // Otrzymano żądanie wysłąnia pliku na serwer
            else if (command == Command.NEED_FILE.asInt()) {
                String relativePath = input.readUTF();
                clientListener.log(">> receiving request for file " + relativePath);
                clientBackend.sendFileData(relativePath);
            }

            // Otrzymano plik
            else if (command == Command.SEND_FILE.asInt()) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();
                clientListener.log(">> receiving file " + relativePath);
                clientBackend.receiveFile(relativePath, modificationTime, size, input);
            }

            // Otrzymano żądanie usunięcia pliku
            else if (command == Command.DELETE_FILE.asInt()) {
                String relativePath = input.readUTF();
                clientListener.log(">> receiving delete request for file " + relativePath);
                clientBackend.deleteFile(relativePath);
            }

            // Otrzymano powiadomienie o aktywnym użytkowniku
            else if (command == Command.USER_ACTIVE.asInt()) {
                String login = input.readUTF();
                clientListener.log(">> receiving active user " + login);
                clientListener.userLogginIn(login);
            }

            // Otrzymano powiadomienie o nieaktywnym użytkowniku
            else if (command == Command.USER_INACTIVE.asInt()) {
                String login = input.readUTF();
                clientListener.log(">> receiving inactive user " + login);
                clientListener.userLogginOut(login);
            }

            // Otrzymano powiadomienie o zatrzymaniu serwera
            else if (command == Command.SERVER_DOWN.asInt()) {
                clientListener.errorOccured();
            }

        } catch (IOException e) {
            clientListener.log("!! IOException occured while receiving data from server");
        }
    }
}