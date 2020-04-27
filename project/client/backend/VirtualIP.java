package project.client.backend;

import java.io.*;
import java.net.*;
import java.nio.*;

/**
 * Klasa umożliwia przydzielanie uruchamianym programom niepowtarzalnych w danej grupie adresów, które rozpoczynają
 * się od wyznaczonego adresu. Działanie klasy polega na umieszczeniu w katalogu z plikami tymczasowymi pliku binarnego
 * z następnym adresem IP który ma zostać przydzielony. Każda aplikacja, która chce uzyskać adres IP nakłada blokadę na
 * ten plik, aby nikt inny jednocześnie go nie odczytywał, rezerwuje dla siebie odczytany adres, inkrementuje o jeden i
 * zwalnia blokadę.
 */
public class VirtualIP {
    private static String tmpDirPath = System.getProperty("java.io.tmpdir");

    /**
     * Metoda przydziela unikalny adres IP.
     * @param groupName Nazwa grupy w obrębie której adres ma być unikalny.
     * @param startingIP Pierwszy, początkowy adres IP.
     * @return Przydzielony adres IP.
     * @throws IOException Wyjątek wyrzucany, gdy nastąpi błąd w operacjach na plikach.
     */
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

    /**
     * Metoda zwiększa adres IP o 1.
     * @param address Adres IP do zwiększenia.
     * @return Zwiększony adres IP.
     */
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
}
