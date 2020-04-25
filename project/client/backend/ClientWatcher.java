package project.client.backend;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ClientWatcher implements Runnable {

    private ClientBackend clientBackend;
    private WatchService watchService = FileSystems.getDefault().newWatchService();
    private Path path;

    private List<String> activeIgnoreList = Collections.synchronizedList(new LinkedList<>());
    private List<String> inactiveIgnoreList = Collections.synchronizedList(new LinkedList<>());

    private ClientListener clientListener;

    public ClientWatcher(ClientBackend clientBackend, ClientListener clientListener) throws IOException {
        this.clientBackend = clientBackend;
        this.clientListener = clientListener;
        path = Paths.get(clientBackend.getDirectory());
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void addIgnore(String relativePath) {
        activeIgnoreList.add(relativePath);
    }

    public void removeIgnore(String relativePath) {
        inactiveIgnoreList.add(relativePath);
        activeIgnoreList.remove(relativePath);
    }

    public void run() {
        try {
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if(event.context()==null)
                        continue;

                    String relativePath = event.context().toString();
                    File file = new File(clientBackend.getDirectory(), relativePath);

                    if(file.isDirectory() || relativePath.startsWith("."))
                        continue;

                    if(activeIgnoreList.contains(relativePath) || inactiveIgnoreList.contains(relativePath)) {
                        continue;
                    }

                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        clientListener.log("## File " + relativePath + " was manually created");
                        clientBackend.sendFileData(relativePath);
                    }

                    else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        clientListener.log("## File " + relativePath + " was manually deleted");
                        clientBackend.sendFileDelete(relativePath);
                    }

                    clientListener.filesUpdated();

                    inactiveIgnoreList.clear();
                }
                key.reset();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
