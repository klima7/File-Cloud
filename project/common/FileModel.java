package project.common;
import javafx.beans.property.*;
import java.util.*;

/**
 * Klasa służy do reprezentacji pliku na liście w bibliotexe JavaFX. Jest on wykorzystywana zarówno przez serwer
 * jak i przez klienta.
 */
public class FileModel {
    private final SimpleStringProperty filename = new SimpleStringProperty();
    private final SimpleLongProperty size = new SimpleLongProperty();
    private final SimpleStringProperty modification = new SimpleStringProperty();

    /**
     * Konstruuje objekt reprezentujący plik o danej nazwie, rozmiarze i dacie modyfikacji.
     * @param filename Nazwa pliku.
     * @param size Rozmiar pliku.
     * @param modification Data modyfikacji pliku.
     */
    public FileModel(String filename, long size, String modification) {
        // Ustawienie przekazanych parametrów
        setFilename(filename);
        setModification(modification);
        setSize(size);
    }

    /**
     * Zwraca nazwę pliku.
     * @return Nazwa pliku.
     */
    public String getFilename() {
        return filename.get();
    }

    /**
     * Zwraca rozmiar pliku
     * @return rozmiar pliku
     */
    public long getSize() {
        return size.get();
    }

    /**
     * Zwraca czas ostatniej modyfikacji pliku w formie tekstowej.
     * @return Czas ostatniej modyfikacji pliku w formie tekstowej.
     */
    public String getModification() {
        return modification.get();
    }

    /**
     * Zmiena nazwe pliku.
     * @param filename Nowa nazwa pliku.
     */
    public void setFilename(String filename) {
        this.filename.set(filename);
    }

    /**
     * Zmienia rozmiar pliku.
     * @param size Nowy rozmiar pliku.
     */
    public void setSize(long size) {
        this.size.set(size);
    }

    /**
     * Zmienia datę modyfikacji pliku.
     * @param modification Nowa data modyfikacji.
     */
    public void setModification(String modification) {
        this.modification.set(modification);
    }

    /**
     * Konwertuje czas z postaci liczby milisekund od "epoki" do czytelnej postaci.
     * @param time Czas w postaci liczby milisekund od epoki.
     * @return Czytelnia reprezentacja czasu.
     */
    public static String convertToReadableTime(long time) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(time));
        String text = calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." +
                calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
        return text;
    }
}
