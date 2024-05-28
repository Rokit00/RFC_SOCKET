package suhun.kim.socket;

public interface SocketService {
    void setSocket(String value);

    void disconnect();

    String logic(String importParam, String importParam1);

    void setServerSocketByKRW();
}
