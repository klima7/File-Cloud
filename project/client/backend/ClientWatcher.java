package project.client.backend;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Klasa obserwuje katalog lokalny użytkownika i reaguje na zmiany w nim. W przypadku pojawienia się ręcznie dodanych
 * plików wysyła je na serwer, a w przypadku skasowania wysyła na serwer żądanie skasowania pliku.
 * Dostępne są również metody, które umożliwiają ignorowanie zmian w katalogu dotyczących danego pliku.
 */
public class ClientWatcher implements Runnable {

    // Parametry przekazane w konstruktorze
    private ClientBackend clientBackend;
    private ClientListener clientListener;

    // Obiekt służący do powiadamiania o zmianach w katalogu
    private WatchService watchService = FileSystems.getDefault().newWatchService();

    // Listy zawierające nazwy plików, których zmiany mają być ignorowane
    private List<String> activeIgnoreList = Collections.synchronizedList(new LinkedList<>());
    private List<String> inactiveIgnoreList = Collections.synchronizedList(new LinkedList<>());

    /**
     * Konstruuje obiekt wykrywający zmiany w katalogu lokalnym i reagujący na nie.
     * @param clientBackend Backend.
     * @throws IOException Zwracany w przypadku, gdy śledzenie katalogu się nie powiedzie.
     */
    public ClientWatcher(ClientBackend clientBackend) throws IOException {
        this.clientBackend = clientBackend;
        this.clientListener = clientBackend.getClientListener();
        Path path = Paths.get(clientBackend.getDirectory());
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Metoda dodaję plik do listy wyjątków. Zmiany dotyczące tego pliku będą ignorowane.
     * @param relativePath Ścieżka do ignorowanego pliku.
     */
    public void addIgnore(String relativePath) {
        activeIgnoreList.add(relativePath);
    }

    /**
     * Metoda usówa plik z listy wyjątków. Zmiany dotyczące tego pliku nie będą już ignorowane.
     * @param relativePath Ścieżka do pliku.
     */
    public void removeIgnore(String relativePath) {
        inactiveIgnoreList.add(relativePath);
        activeIgnoreList.remove(relativePath);
    }

    /**
     * Zadanie, które polega na ciągłym nadzorowaniu katalogu i reagowaniu na zmiany. Jest ono automatycznie
     * uruchamiane w konstruktorze w osobnym wątku.
     */
    public void run() {
        try {
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if(event.context()==null)
                        continue;

                    String relativePath = event.context().toString();
                    File file = new File(clientBackend.getDirectory(), relativePath);

                    // Ignorowanie ukrytych plików
                    if(file.isDirectory() || relativePath.startsWith("."))
                        continue;

                    // Ignorowanie plików z listy
                    if(activeIgnoreList.contains(relativePath) || inactiveIgnoreList.contains(relativePath)) {
                        continue;
                    }

                    // Gdy stworzono nowy plik
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        clientListener.log("## File " + relativePath + " was manually created");
                        clientBackend.sendFileData(relativePath);
                    }

                    // Gdy zmodyfikowano plik
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        clientListener.log("## File " + relativePath + " was manually modified");
                        clientBackend.sendFileData(relativePath);
                    }

                    // Gdy usunięto plik
                    else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        clientListener.log("## File " + relativePath + " was manually deleted");
                        clientBackend.sendFileDelete(relativePath);
                    }

                    // Powiadomienie frontendu o dokonaniu zmian w katalogu
                    clientListener.filesUpdated();
                    inactiveIgnoreList.clear();
                }
                key.reset();
            }
            // Standardowy sposób zakończenia wątku (wykonanie n
        } catch(InterruptedException e) {}
    }
}
