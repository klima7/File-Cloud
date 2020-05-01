package project.client.backend;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Klasa jest zadaniem, które akceptuje wszystkie połączenia pojawiające się na danym sockecie i dla każdego
 * połączenia uruchamia zadanie ClientReader w osobnym wątku, które odczytuje treść przychodzącej wiadomości i
 * odpowiednią na nią reaguje.
 */
class ClientAccepter implements Runnable {

    // Parametry przekazane w konstruktorze
    private ServerSocket serverSocket;
    private ClientBackend clientBackend;
    private ClientListener clientListener;

    // Pula wątków w której umieszczane są zadania ClientReader obsługujące przychodzące połączenia
    private ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Konstruuje obiekt akceptujący połączenia na przekazanym sockecie. Wymaga obiektu backendu, aby móc reagować
     * na przychodzące od klientów żądania.
     * @param serverSocket Gniazdo na którym przychodzące połączenia są akceptowane.
     * @param clientBackend Backend wymagany do realizacji przychodzących żądań.
     * @param clientListener Obiekt słuchacza, którego metody są wysoływane w odpowiedzi na zachodzące zdarzenia.
     */
    public ClientAccepter(ServerSocket serverSocket, ClientBackend clientBackend, ClientListener clientListener) {
        this.serverSocket = serverSocket;
        this.clientBackend = clientBackend;
        this.clientListener = clientListener;
    }

    /**
     * Zadanie polegające na akceptowaniu połączeń i uruchamianiu dla każdego procedury jego obsługi
     */
    public void run() {
        try {
            while(!Thread.interrupted()) {
                Socket socket = serverSocket.accept();
                executor.execute(new ClientReader(socket, clientBackend, clientListener));
            }
        } catch(IOException e) {
            executor.shutdownNow();
            try { executor.awaitTermination(1, TimeUnit.HOURS); }
            catch(InterruptedException f) { clientListener.log("!! Error occured while accepting connection"); }
        }
    }
}