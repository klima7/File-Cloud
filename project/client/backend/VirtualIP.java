package project.client.backend;

import java.io.*;
import java.net.*;
import java.nio.*;

public class VirtualIP {
    private static String tmpDirPath = System.getProperty("java.io.tmpdir");

    public static InetAddress allocateIP(String groupName, String startingIP) throws IOException {
        File ipFile = new File(tmpDirPath, groupName);

        // Gdy plik nie istnieje
        if(!ipFile.exists()) {

            // Tworzenie i blokowanie pliku
            ipFile.createNewFile();
            try (RandomAccessFile rc = new RandomAccessFile(ipFile, "rw")) {
                rc.getChannel().lock();

                // Inicjowanie pliku
                InetAddress address = InetAddress.getByName(startingIP);
                byte[] bytes = address.getAddress();
                for (byte aByte : bytes)
                    rc.writeByte(aByte);
            }
        }

        // Adres do zwrócenia, który zostanie zaraz zainicjowany
        InetAddress allocatedAddress = null;

        // Odczyt pliku w celu przyznania adresu
        try (RandomAccessFile rc = new RandomAccessFile(ipFile, "rw")) {
            rc.getChannel().lock();

            // Odczyt adresu z pliku
            byte[] bytes = new byte[4];
            rc.read(bytes);
            allocatedAddress = InetAddress.getByAddress(bytes);

            // Zwiększenie adresu
            InetAddress nextAddress = incrementAddress(allocatedAddress);

            // Zapis adresu spowrotem do pliku
            rc.seek(0L);
            bytes = nextAddress.getAddress();
            rc.write(bytes);
        }

        return allocatedAddress;
    }

    private static InetAddress incrementAddress(InetAddress address) {
        int asInt = ByteBuffer.wrap(address.getAddress()).getInt();
        asInt++;

        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        buffer.putInt(asInt);
        buffer.rewind();
        byte[] newBytes = new byte[4];
        buffer.get(newBytes);

        try {
            InetAddress incremented = InetAddress.getByAddress(newBytes);
            return incremented;
        } catch(UnknownHostException e) {
            return null;
        }
    }

    // Testowy fragment kodu
    public static void main(String[] args) throws IOException {
        for(int i=0; i<10; i++) {
            InetAddress address = VirtualIP.allocateIP("Network", "127.0.0.1");
            System.out.println(address);
        }
    }
}
