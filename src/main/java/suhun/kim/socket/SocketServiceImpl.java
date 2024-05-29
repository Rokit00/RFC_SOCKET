package suhun.kim.socket;

import suhun.kim.util.Crypt;
import suhun.kim.util.PropertiesUtil;
import suhun.kim.util.TripleDESImpl;
import com.sap.conn.jco.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;


public class SocketServiceImpl extends Thread implements SocketService {
    private static final Logger log = LoggerFactory.getLogger(SocketServiceImpl.class);
    private static final Properties properties = PropertiesUtil.getProperties();
    private final Crypt crypt = null;
    private Socket socket;

    @Override
    public void setSocket(String value) {
        try {
            switch (value) {
                case "WON":
                    socket = new Socket(properties.getProperty("SOCKET.IP"), Integer.parseInt(properties.getProperty("SOCKET.PORT.KRW")));
                    log.info("CONNECTED TO KRW SOCKET {}:{}", properties.getProperty("SOCKET.IP"), properties.getProperty("SOCKET.PORT.KRW"));
                    break;
                case "KEB":
                    socket = new Socket(properties.getProperty("SOCKET.IP"), Integer.parseInt(properties.getProperty("SOCKET.PORT.KEB")));
                    log.info("CONNECTED TO KEB SOCKET {}:{}", properties.getProperty("SOCKET.IP"), properties.getProperty("SOCKET.PORT.KEB"));
                    break;
                case "BILL":
                    socket = new Socket(properties.getProperty("SOCKET.IP"), Integer.parseInt(properties.getProperty("SOCKET.PORT.BILL")));
                    log.info("CONNECTED TO BILL SOCKET {}:{}", properties.getProperty("SOCKET.IP"), properties.getProperty("SOCKET.PORT.BILL"));
                    break;
            }
        } catch (IOException e) {
            log.error("FAILED TO CONNECT SOCKET: {}", e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
                log.info("DISCONNECTED TO VAN");
            }
        } catch (IOException e) {
            log.error("FAILED TO DISCONNECT FROM VAN: {}", e.getMessage());
        }
    }

    @Override
    public String logic(String importParam, String importParam1) {
        long startTime = System.currentTimeMillis();
        try {
            switch (importParam1) {
                case "WON":
                case "BILL": {
                    setSocket(importParam1);
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    byte[] toBytes = truncateToBytes(importParam, 300);
                    log.info("SAP -> DEMON: [{}] [{}byte] [{}]", importParam1, toBytes.length, importParam);

                    outputStream.write(toBytes);
                    outputStream.flush();

                    log.info("[SUCCESS] DEMON -> VAN");

                    DataInputStream reader = new DataInputStream(socket.getInputStream());

                    byte[] receivedBytes = new byte[300];
                    reader.readFully(receivedBytes);
                    String receivedMessage = new String(receivedBytes, "EUC-KR");

                    if (receivedMessage.isEmpty() || receivedMessage.equals("NULL")) {
                        log.info("NO DATA RECEIVED FROM VAN");
                        return "F";
                    }

                    log.info("VAN -> DEMON: [{}] [{}byte] [{}]",importParam1, receivedBytes.length, receivedMessage);
                    setSendToSap(receivedMessage, importParam1);

                    long endTime = System.currentTimeMillis() - startTime;
                    log.info("[SUCCESS] DEMON -> SAP ({}sec)", endTime * 0.001);

                    return "S";
                }
                case "KEB": {
                    setSocket(importParam1);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    byte[] toBytes = truncateToBytes(importParam, 2000);
                    log.info("SAP -> DEMON: [{}] [{}byte] [{}]", importParam1, toBytes.length, importParam);

                    dataOutputStream.write(toBytes);
                    dataOutputStream.flush();

                    log.info("[SUCCESS] DEMON -> VAN");

                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                    byte[] receivedBytes = new byte[2000];
                    dataInputStream.readFully(receivedBytes);
                    String receivedMessage = new String(receivedBytes, "EUC-KR");

                    if (receivedMessage.isEmpty() || receivedMessage.equals("NULL")) {
                        log.info("NO DATA RECEIVED FROM VAN");
                        return "F";
                    }

                    log.info("VAN -> DEMON: [{}] [{}byte] [{}]",importParam1, receivedBytes.length, receivedMessage);
                    setSendToSap(receivedMessage, importParam1);

                    long endTime = System.currentTimeMillis() - startTime;
                    log.info("[SUCCESS] DEMON -> SAP ({}sec)", endTime * 0.001);

                    return "S";
                }
                default:
                    log.info("INCORRECT STRING TYPE");
                    return "F";
            }
        } catch (IOException e) {
            log.error("DATA VALUE: {}", e.getMessage());
            return "F";
        } finally {
            disconnect();
        }
    }

    @Override
    public void setServerSocketByKRW() {
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(properties.getProperty("SOCKET.PORT.KRW")));
            while (true) {
                socket = serverSocket.accept();

                byte[] bytes = new byte[300];
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                dataInputStream.readFully(bytes);
                String receiveMessage = new String(bytes, "EUC-KR");
                try {
                    JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                    JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.KRW"));
                    JCoFunction jCoFunction1 = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.BILL"));
                    jCoFunction.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.KRW"), receiveMessage);
                    jCoFunction1.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.BILL"), receiveMessage);
                    jCoFunction.execute(jCoDestination);
                    log.info("[RECEIVE KRW DATA] VAN -> DEMON [{}byte] [{}]", receiveMessage.getBytes().length, receiveMessage);
                } catch (JCoException e) {
                    log.error("ERROR KRW SERVER SOCKET: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("RECEIVED KRW DATA VALUE [{}]", e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(properties.getProperty("SOCKET.PORT.KEB")));
            while (true) {
                socket = serverSocket.accept();

                byte[] bytes = new byte[2000];
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                dataInputStream.readFully(bytes);
                String receiveMessage = new String(bytes, "EUC-KR");
                try {
                    JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                    JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.KEB"));
                    jCoFunction.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.KEB"), receiveMessage);
                    jCoFunction.execute(jCoDestination);
                    log.info("[RECEIVE KEB DATA] VAN -> DEMON [{}byte] [{}]", receiveMessage.getBytes().length, receiveMessage);
                } catch (JCoException e) {
                    log.error("ERROR KEB SERVER SOCKET: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("RECEIVED KEB DATA VALUE [{}]", e.getMessage());
        }
    }

    private byte[] truncateToBytes(String value, int len) throws UnsupportedEncodingException {
        byte[] bytes = value.getBytes("EUC-KR");

        if (bytes.length <= len) {
            return value.getBytes();
        }

        int truncatedLength = len;
        while (true) {
            if ((bytes[truncatedLength] & 0xC0) != 0x80) {
                break;
            }
            truncatedLength--;
        }

        byte[] truncatedBytes = new byte[truncatedLength];
        System.arraycopy(bytes, 0, truncatedBytes, 0, truncatedLength);

        return truncatedBytes;
    }

    private void setSendToSap(String value, String type) {
        switch (type) {
            case "KRW":
                try {
                    JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                    JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.KRW"));
                    jCoFunction.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.KRW"), value);
                    jCoFunction.execute(jCoDestination);
                } catch (JCoException e) {
                    log.error("DEMON(KRW) -> SAP: {}", e.getMessage());
                }
                break;
            case "KEB":
                try {
                    JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                    JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.KEB"));
                    jCoFunction.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.KEB"), value);
                    jCoFunction.execute(jCoDestination);
                } catch (JCoException e) {
                    log.error("DEMON(KEB)-> SAP: {}", e.getMessage());
                }
                break;
            case "BILL":
                try {
                    JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                    JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.BILL"));
                    jCoFunction.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.BILL"), value);
                    jCoFunction.execute(jCoDestination);
                } catch (JCoException e) {
                    log.error("DEMON(BILL)-> SAP: {}", e.getMessage());
                }
                break;
        }
    }
}