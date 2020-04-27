package project.client.backend;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import project.common.*;

/**
 * Klasa realizuje zadania klienta, czy komunikację z serwerem.
 */
public class ClientBackend {
    public static final String IP_GROUP = "Virtual Network1";
    public static final String IP_START = "127.0.0.2";

    // Parametry przekazane w konstruktorze
    private int port;
    private String login;
    private String directory;
    private ClientListener clientListener;

    // Unikalny wirtualny adres IP
    private InetAddress addresIP;

    // Socket serwera na którym oczekuje się na połączenia
    private ServerSocket serverSocket;

    // Wątek, który działa w tle i akceptuje wszystkie przychodzące połączenia
    private Thread acceptingThread;

    // Pula wątków w której umieszczane są wszystkie zadania wysyłania komunikatów do serwera
    private ExecutorService executor = Executors.newCachedThreadPool();

    // Obiekt służący do wykrywania zmian w katalogu lokalnym
    private ClientWatcher clientWatcher;

    /**
     * Konstruuje obiekt backendu klienta.
     * @param login Login użytownika.
     * @param directory Ścieżka do katalogu lokalnego użytkonika.
     * @param port Numer portu na którym ma odbywać się komunikacja.
     * @param clientListener Obiekt słuchacza, którego metody mają być wywoływane w odpowiedzi na zachodzące zdarzenia.
     * @throws IOException Wyjątek wyrzucany gdy nastąpi problem podczas operacji wejścia/wyjścia
     */
    public ClientBackend(String login, String directory, int port, ClientListener clientListener) throws IOException {
        // Zapamiętanie parametrów
        this.login = login;
        this.directory = directory;
        this.port = port;
        this.addresIP = VirtualIP.allocateIP(IP_GROUP, IP_START);
        this.clientListener = clientListener;

        // Stworzenie menedżera klientów
        clientWatcher = new ClientWatcher(this, clientListener);

        // Wysłanie logu
        clientListener.log("## Client running on port " + port + ", login is " + login);
    }

    /**
     * Metoda zwraca ścieżkę do katalogu lokalnego użytkownika.
     * @return Ścieżka do katalogu lokalnego użytkownika.
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
     * Metoda zwraca adres IP Klienta.
     * @return Adres IP klienta.
     */
    public InetAddress getIP() {
        return addresIP;
    }

    /**
     * Metoda zwraca numer portu służącego do komunikacji.
     * @return Numer portu.
     */
    public int getPort() {
        return port;
    }

    /**
     * Metoda zwraca obiekt słuchacza, którego metody są wysoływane w odpowiedzi na zdarzenia.
     * @return Obiekt słuchacza.
     */
    public ClientListener getClientListener() {
        return clientListener;
    }

    /**
     * Metoda zwraca adres IP klienta.
     * @return adres IP klienta.
     */
    public InetAddress getAddressIP() {
        return addresIP;
    }

    /**
     * Metoda uruchamia mechanizmy klienta odpowiedzialne za komunikację z serwerem.
     * @throws IOException Wyjątek zwracany w przypadku problemu z utworzeniem Socketu.
     */
    public void start() throws IOException {
        sendLogin(login);
        serverSocket = new ServerSocket(port, 256, addresIP);
        acceptingThread = new Thread(new ClientAccepter(serverSocket, this, clientListener));
        acceptingThread.start();
    }

    /**
     * Metoda zatrzymuje mechanizmy klienta odpowiedzialne za komunikację.
     */
    public void stop() {
        try {
            sendLogout(login);
            serverSocket.close();
            acceptingThread.interrupt();
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.HOURS);
        }
        catch(Exception e) {
            clientListener.errorOccured();
        }
    }

    /**
     * Metoda wysyła do serwera komunikat o logowaniu.
     * @param login Login użytkownika.
     */
    public void sendLogin(String login) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending login request");
                stream.writeInt(Command.LOGIN.asInt());
                stream.writeUTF(login);
            }
        });
    }

    /**
     * Metoda wysyła do serwera komunikat o wylogowaniu.
     * @param login Login użytkownika.
     */
    public void sendLogout(String login) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending logout request");
                stream.writeInt(Command.LOGOUT.asInt());
                stream.writeUTF(login);
            }
        });
    }

    /**
     * Metoda wysyła powiadomienie o pliku jaki istnieje w katalogu lokalnym.
     * @param relativePath Ścieżka do pliku w katalogu lokalnym.
     */
    public void sendFileCheck(String relativePath) {
        // Odczytanie daty modyfikacji
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();

        // Upewnienie się, że nie jest to katalog
        if(file.isDirectory())
            return;

        // Wysłanie wiadomości
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending file advertisement for " + relativePath);
                stream.writeInt(Command.CHECK_FILE.asInt());
                stream.writeUTF(relativePath);
                stream.writeLong(modificationTime);
            }
        });
    }

    /**
     * Metoda wysyła do serwera powiadomienia o wszystkich plikach jakie istnieją w katalogu lokalnym.
     */
    public void sendFileCheckAll() {
        String[] list = new File(directory).list();
        for(String name : list)
            sendFileCheck(name);
    }

    /**
     * Metoda wysyła do serwera proźbę o przesłanie danego pliku.
     * @param relativePath Ścieżka do pliku, którego tyczy się proźba.
     */
    public void sendFileRequest(String relativePath) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending send request for file " + relativePath);
                stream.writeInt(Command.NEED_FILE.asInt());
                stream.writeUTF(relativePath);
            }
        });
    }

    /**
     * Metoda wysyła na serwer żądanie usunięcia pliku.
     * @param relativePath Nazwa pliku, którego tyczy się żądanie.
     */
    public void sendFileDelete(String relativePath) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending delete request for file " + relativePath);
                stream.writeInt(Command.DELETE_FILE.asInt());
                stream.writeUTF(relativePath);
            }
        });
    }

    /**
     * Metoda wysyła plik na na serwer do swojego zdalnego katalogu lub do innego użytkownika.
     * @param relativePath Ścieżka do wysyłanego pliku.
     * @param login null jeżeli plik jest wysyłany do własnego katalogu zdanego, lub login użytkownika do którego plik ma być wysłany.
     */
    public void sendFileData(String relativePath, String login) {
        // Odczytanie czasu modyfikacji i rozmiaru pliku.
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();
        long size = file.length();

        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                // Gdy plik jest wysyłany do własnego katalogu
                if(login == null) {
                    clientListener.log("<< sending file " + relativePath);
                    stream.writeInt(Command.SEND_FILE.asInt());
                }

                // Gdy plik jest wysyłany do innego użytkownika
                else {
                    clientListener.log("<< sending file " + relativePath + " to " + login);
                    stream.writeInt(Command.SEND_TO_USER.asInt());
                    stream.writeUTF(login);
                }

                // Wysłanie metainformacji
                stream.writeUTF(relativePath);
                stream.writeLong(modificationTime);
                stream.writeLong(size);

                // Wysłanie danych pliku
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
     * Metoda wysyła plik na serwer, do katalogu zdalnego użytkownika.
     * @param relativePath Ścieżka do wysyłanego pliku.
     */
    public void sendFileData(String relativePath) {
        sendFileData(relativePath, null);
    }

    /**
     * Metoda tworzy w katalogu lokalnym plik o podanej nazwie i dacie modyfikacji oraz wczytuje do niego dane pobrane ze strumienia.
     * @param relativePath Nazwa pliku do utworzenia.
     * @param modificationTime Data modyfikacji pliku.
     * @param size Ilość bajtów, która ma zostać wczytana ze strumienia do pliku.
     * @param input Strumień z którego dane będą wczytywane do pliku.
     */
    public void receiveFile(String relativePath, long modificationTime, long size, DataInputStream input) {
        // Ignorowanie zmian w katalogu lokalnym odnoście tworzonego pliku
        clientWatcher.addIgnore(relativePath);

        File newFile = new File(directory, relativePath);
        File parent = newFile.getParentFile();

        // Tworzenie katalogu jeżeli nie istnieje
        if(!parent.exists())
            parent.mkdirs();

        // Kasowanie pliku jeżeli istnieje
        if(newFile.exists())
            newFile.delete();

        // Tworzenie pustego pliku
        try { newFile.createNewFile(); }
        catch(IOException e) {
            clientListener.log("!! Unable do create file in local directory");
            return;
        }

        // Wczytanie danych do pliku bajt po bajcie
        try(FileOutputStream output = new FileOutputStream(newFile)) {
            for (long pos = 0; pos < size; pos++) {
                int aByte = input.read();
                output.write(aByte);
            }
        } catch(IOException e) {
            clientListener.log("!! IOException occured when receiving file");
            return;
        }

        // Ustawienie czasu modyfikacji
        newFile.setLastModified(modificationTime);

        // Aktywowanie obserwowania pliku
        clientWatcher.removeIgnore(relativePath);

        // Powiadomienie za pomocą listenera o dokonaniu zmian w katalogu
        clientListener.filesUpdated();
    }

    /**
     * Metoda sprawdza, czy plik określony ścieżką jest aktualniejszy od przekazanej daty modyfikacji.
     * @param relativePath Ścieżka do pliku w katalogu lokalnym, którego aktualność jest sprawdzana.
     * @param otherModificationTime Czas modyfikacji do porównania.
     * @return true jeżeli plik określony ścieżką jest aktualny, false w przeciwnym wypadku.
     */
    public boolean isFileUpToDate(String relativePath, long otherModificationTime) {
        clientListener.log("## Checking if file " + relativePath + " is up to date");
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();
        if(modificationTime < otherModificationTime)
            return false;
        else
            return true;
    }

    /**
     * Metoda usówa plik z katalogu klienta, w taki sposób, że ClientWatcher nie dostrzega zmiany w katalogu lokalnym
     * @param relativePath Ścieżka do usówanego pliku.
     */
    public void deleteFile(String relativePath) {
        clientListener.log("## Deleting file " + relativePath);
        clientWatcher.addIgnore(relativePath);
        File file = new File(directory, relativePath);
        file.delete();
        clientWatcher.removeIgnore(relativePath);

        // Powiadomienie listenera o dokonaniu zmian w katalogu lokalnym.
        clientListener.filesUpdated();
    }

     // Jest to klasa pomocnicza, która umożliwia uniknięcie pisania powtarzającego się kodu w metodach wysyłąjących komendy do serwera.
    private abstract class SendWrapper implements Runnable {
        public void run() {
            executor.execute(() ->
            {
                try (Socket socket = new Socket(InetAddress.getByName(ImportantConstants.SERVER_ADDRESS), port, addresIP, 0)) {
                    DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                    send(stream);
                } catch (IOException e) {
                    clientListener.errorOccured();
                }
            });
        }

         // Metoda w której powinno wystąpić wysyłamie komunikatu za pomocą strumienia stream w klasach pochodnych
        abstract void send(DataOutputStream stream) throws IOException;
    }
}
