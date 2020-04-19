package project.server;

import static project.common.Constants.*;
import java.io.*;
import java.util.concurrent.*;

public class ServerApp {

    public static final String SERVER_DIRECTORY = "/home/klima7/SERVER";

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerBackend backend = new ServerBackend(SERVER_DIRECTORY, PORT);
        backend.startServer();
        TimeUnit.HOURS.sleep(1);
    }
}
