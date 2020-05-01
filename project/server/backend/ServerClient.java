package project.server.backend;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import project.common.*;

/**
 * Klasa reprezentuje klienta, w sposób w jaki widzi go serwer.
 */
public class ServerClient {

    // Parametry przekazywane w konstruktorze
    private InetAddress addressIP;
    private int port;
    private ServerUser user;
    private ServerListener serverListener;

    // Pula wątków w której umieszczane są wszystkie zadania wysyłania związane z klientem
    private ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Konstruktor tworzy obiekt klienta o podanych parametrach.
     * @param addressIP Adres IP klienta
     * @param user Obiekt użytkownika do którego należy klient.
     * @param port Numer port za pomocą którego klient będzie się komunikował.
     * @param serverListener Obiekt słuchacza.
     */
    public ServerClient(InetAddress addressIP, ServerUser user, int port, ServerListener serverListener) {
        // Zapamiętanie parametrów
        this.addressIP = addressIP;
        this.port = port;
        this.user = user;
        this.serverListener = serverListener;
    }

    /**
     * Metoda zwraca obiekt użytkownika do którego należy klient.
     * @return Obiekt użytkownika do którego należy klient.
     */
    public ServerUser getUser() {
        return user;
    }

    /**
     * Metoda zwraca adres IP klienta.
     * @return adres IP klienta.
     */
    public InetAddress getIpAddress() {
        return addressIP;
    }

    /**
     * Metoda zwraca numer portu za pomocą którego klient się komunikuje.
     * @return Numer portu.
     */
    public int getPort() {
        return port;
    }

    /**
     * Metoda zwraca obiekt słuchacza.
     * @return Obiekt słuchacza.
     */
    public ServerListener getServerListener() {
        return serverListener;
    }

    /**
     * Metoda wykonuje zadania niezbędne, gdy klient się wyloguje, czyli zamyka związaną z nim pulę wątków.
     */
    public void logout() {
        executor.shutdown();
    }

    /**
     * Metoda wysyła klientowi wiadomość o udanym logowaniu.
     */
    public void sendLoginSuccess() {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                serverListener.log("<< Sending login success to " + ServerClient.this.toString());
                stream.writeInt(Command.LOGIN_SUCCESS.asInt());
            }
        });
    }

    /**
     * Metoda wysyła klientowi plik.
     * @param relativePath Ścieżka do wysyłanego pliku względem katalogu użytkownika.
     */
    public void sendFile(String relativePath) {
        // Odczytanie czasu modyfikacji i rozmiaru pliku
        File file = new File(user.getDirectory(), relativePath);
        long modificationTime = file.lastModified();
        long size = file.length();

        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                // Powiadomienie o wysyłaniu pliku
                serverListener.log("<< Sending file " + relativePath + " to " + ServerClient.this.toString());

                // Wysłanie nagłówka
                stream.writeInt(Command.SEND_FILE.asInt());
                stream.writeUTF(relativePath);
                stream.writeLong(modificationTime);
                stream.writeLong(size);

                // Wysyłanie zawartości pliku
                try(FileInputStream input = new FileInputStream(file)) {
                    for(long pos=0; pos<size; pos++) {
                        int aByte = input.read();
                        stream.write(aByte);
                        stream.flush();
                    }
                }
            }
        });
    }

    /**
     * Metoda wysyła klientowi powiadomienie o pliku istniejącym na serwerze.
     * @param relativePath Ścieżka do pliku względem katalogu użytkownika.
     */
    public void sendAdvertisement(String relativePath) {
        // Odczytanie daty modyfikacji
        File file = new File(user.getDirectory(), relativePath);
        long modificationTime = file.lastModified();

        // Ignorowanie katalogów
        if(file.isDirectory())
            return;

        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                serverListener.log("<< Sending advertisement about file " + relativePath + " to " + ServerClient.this.toString());
                stream.writeInt(Command.CHECK_FILE.asInt());
                stream.writeUTF(relativePath);
                stream.writeLong(modificationTime);
            }
        });
    }

    /**
     * Metoda wysyła klientowi powiadomienia o wszystkich plikach, które istnieją w jego katalogu na serwerze.
     */
    public void sendAdvertisements() {
        String[] list = new File(user.getDirectory()).list();
        for(String name : list)
            sendAdvertisement(name);
    }

    /**
     * Metoda wysyła klientowi żądanie, aby on przesłał na serwer dany plik.
     * @param relativePath Ścieżka do pliku względem katalogu użytkownika.
     */
    public void sendRequest(String relativePath) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                serverListener.log("<< Sending send request for file " + relativePath + " to " + ServerClient.this.toString());
                stream.writeInt(Command.NEED_FILE.asInt());
                stream.writeUTF(relativePath);
            }
        });
    }

    /**
     * Metoda wysyła klientowi żądanie usunięcia pliku.
     * @param relativePath Ścieżka do pliku względem katalogu użytkownika.
     */
    public void sendDelete(String relativePath) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                serverListener.log("<< Sending delete request for file " + relativePath + " to " + ServerClient.this.toString());
                stream.writeInt(Command.DELETE_FILE.asInt());
                stream.writeUTF(relativePath);
            }
        });
    }

    /**
     * Wysłanie klientowi powiadomienie o aktywnym użytkowniku
     * @param login Login aktywnego użytkownika
     */
    public void sendUserActive(String login) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                serverListener.log("<< Sending active user notification to " + ServerClient.this.toString());
                stream.writeInt(Command.USER_ACTIVE.asInt());
                stream.writeUTF(login);
            }
        });
    }

    /**
     * Wysłanie klientowi powiadomienie o nieaktywnym użytkowniku
     * @param login Login nieaktywnego użytkownika
     */
    public void sendUserInactive(String login) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                serverListener.log("<< Sending inactive user notification to " + ServerClient.this.toString());
                stream.writeInt(Command.USER_INACTIVE.asInt());
                stream.writeUTF(login);
            }
        });
    }

    /**
     * Metoda wysyła klientowi powiadomienie o zatrzymaniu serwera.
     */
    public void sendServerDown() {
        serverListener.log("<< Sending server shutdown info to " + ServerClient.this.toString());
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(Command.SERVER_DOWN.asInt());
            }
        });
    }

    /**
     * Metoda zwraca tekstową reprezentacje klienta postaci adresIP(login).
     * @return Tekstowa reprezentacja klienta.
     */
    @Override
    public String toString() {
        return addressIP.getHostName() + "(" + user.getLogin() + ")";
    }

    // Klasa pomocnicza pozwalająca uniknąć powtarzającego się kodu w metodach wysyłających
    private abstract class SendWrapper implements Runnable {
        public void run() {
            executor.execute(() ->
            {
                try (Socket socket = new Socket(addressIP, port)) {
                    DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                    send(stream);
                } catch (IOException e) {
                    serverListener.log("!! Error occured while sending message");
                }
            });
        }

        // Metoda w której powinno wystąpić wysyłamie komunikatu za pomocą strumienia stream w klasach pochodnych
        abstract void send(DataOutputStream stream) throws IOException;
    }
}
