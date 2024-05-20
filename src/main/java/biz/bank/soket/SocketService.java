package biz.bank.soket;

public interface SocketService {
    void setKRWSocket();
    void setKEBSocket();
    void disConnect();
    String logic(String importParam, String importParam1);
}
