package project.common;

public interface Listener {

    void activeUsersChanged(String[] users);
    void fileSendingProgressed(String file, long progress, long size);
    void fileReceivingProgressed(String file, long progress, long size);
    void localDirectoryChanged();
}
