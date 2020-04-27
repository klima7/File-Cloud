package project.server.backend;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Klasa reprezentująca zadanie które akceptuje wszystkie przychodzące na dany socket połączenia
 * oraz dla każdego takiego połączenia uruchamia w osobnym wątki procedure jego obsługi.
 * Konstruktor tej klasy wymaga objektu menadżera klientów ServerClientManager, ponieważ odbierane
 * komunikaty mogą polegać na dodaniu nowego użytkownika lub wylogowaniu użytkownika.
 */
class ServerAccepter implements Runnable {

    // Parametry przekazane w konstruktorze
    private ServerSocket serverSocket;
    private ServerClientsManager clientsManager;
    private ServerListener serverListener;

    // Pula w której umieszczane są wątki obsługujące poszczególne połączenia
    private ExecutorService executor = Executors.newCachedThreadPool();

    public ServerAccepter(ServerSocket serverSocket, ServerClientsManager clientsManager, ServerListener serverListener) {
        // Zapamiętanie wszystkich przekazanych parametrów
        this.serverSocket = serverSocket;
        this.clientsManager = clientsManager;
        this.serverListener = serverListener;
    }

    /**
     * Zadanie polegające na akceptowaniu połączeń i uruchamianiu dla każdego procedury jego obsługi
     */
    public void run() {
        try {
            while(!Thread.interrupted()) {
                Socket socket = serverSocket.accept();
                executor.execute(new ServerReader(socket, clientsManager, serverListener));
            }
            // Wyjątek ten jest zgłaszany gdy socket zostanie zamknięty, co dokonuje się podczas zatrzymywania serwera.
        } catch(IOException e) {
            // Zamknięcie puli wątków i oczkiwanie, aż już umieszczone wątki się zakończą
            executor.shutdownNow();
            try { executor.awaitTermination(1, TimeUnit.HOURS); }
            // Wyjątek bardzo mało prawdopodobny
            catch(InterruptedException f) {}
        }
    }
}