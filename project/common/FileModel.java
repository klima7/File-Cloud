package project.common;
import javafx.beans.property.*;

public class FileModel {
    private final SimpleStringProperty filename = new SimpleStringProperty();
    private final SimpleLongProperty size = new SimpleLongProperty();
    private final SimpleLongProperty modification = new SimpleLongProperty();

    public FileModel(String filename, long size, long modification) {
        setFilename(filename);
        setModification(modification);
        setSize(size);
    }

    public String getFilename() {
        return filename.get();
    }

    public long getSize() {
        return size.get();
    }

    public long getModification() {
        return modification.get();
    }

    public void setFilename(String filename) {
        this.filename.set(filename);
    }

    public void setSize(long size) {
        this.size.set(size);
    }

    public void setModification(long modification) {
        this.modification.set(modification);
    }
}
