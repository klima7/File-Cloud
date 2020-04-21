package project.client.backend;

import static project.common.Constants.*;
import java.io.*;
import java.util.concurrent.*;

public class ClientApp {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Here");
        if(args.length<2) {
            System.err.println("Invalid arguments count");
            System.exit(1);
        }

        ClientBackend backend = new ClientBackend(args[0], args[1], PORT);
        backend.start();
        TimeUnit.DAYS.sleep(1);
    }
}
