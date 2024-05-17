package biz.bank.util;

public interface Crypt {
    byte[] encrypt(String str);

    byte[] decrypt(byte[] encrypted);
}
