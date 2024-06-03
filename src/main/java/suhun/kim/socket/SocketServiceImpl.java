package suhun.kim.socket;

import suhun.kim.util.Crypt;
import suhun.kim.util.PropertiesUtil;
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
        long startTime = System.currentTimeMillis();
        try {
            switch (value) {
                case "WON":
                    socket = new Socket(properties.getProperty("SOCKET.IP"), Integer.parseInt(properties.getProperty("SOCKET.PORT.KRW")));
                    log.info("CONNECTED TO KRW SOCKET {}:{} ({}sec)", properties.getProperty("SOCKET.IP"), properties.getProperty("SOCKET.PORT.KRW"), (System.currentTimeMillis() - startTime) * 0.001);
                    break;
                case "KEB":
                    socket = new Socket(properties.getProperty("SOCKET.IP"), Integer.parseInt(properties.getProperty("SOCKET.PORT.KEB")));
                    log.info("CONNECTED TO KEB SOCKET {}:{} ({}sec)", properties.getProperty("SOCKET.IP"), properties.getProperty("SOCKET.PORT.KEB"), (System.currentTimeMillis() - startTime) * 0.001);
                    break;
                case "BILL":
                    socket = new Socket(properties.getProperty("SOCKET.IP"), Integer.parseInt(properties.getProperty("SOCKET.PORT.BILL")));
                    log.info("CONNECTED TO BILL SOCKET {}:{} ({}sec)", properties.getProperty("SOCKET.IP"), properties.getProperty("SOCKET.PORT.BILL"), (System.currentTimeMillis() - startTime) * 0.001);
                    break;
                default:
                    log.info("INCORRECT TYPE");
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
                log.debug("DISCONNECTED TO VAN");
            }
        } catch (IOException e) {
            log.error("FAILED TO DISCONNECT FROM VAN: {}", e.getMessage());
        }
    }

    @Override
    public String logic(String importParam, String importParam1) {
        long startTime = System.currentTimeMillis();
        try {
            setSocket(importParam1);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            byte[] sendByte = new byte[0];
            switch (importParam1) {
                case "WON":
                case "BILL":
                    sendByte = new byte[300];
                    System.arraycopy(importParam1.getBytes("EUC-KR"), 0 , sendByte, 0, importParam1.getBytes("EUC-KR").length);
                    outputStream.write(sendByte);
                    outputStream.flush();
                    break;
                case "KEB":
                    sendByte = new byte[2000];
                    System.arraycopy(importParam1.getBytes("EUC-KR"), 0 , sendByte, 0, importParam1.getBytes("EUC-KR").length);
                    outputStream.write(sendByte);
                    outputStream.flush();
                    break;
            }

            log.info("[RFC] DEMON -> VAN: [{}] [{}byte] [{}] ({}sec)", importParam1, sendByte.length, importParam, (System.currentTimeMillis() - startTime) * 0.001);

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            byte[] receivedBytes = new byte[0];
            switch (importParam1) {
                case "WON":
                case "BILL":
                    receivedBytes = new byte[300];
                    break;
                case "KEB":
                    receivedBytes = new byte[2000];
                    break;
            }
            dataInputStream.readFully(receivedBytes);
            String receivedMessage = new String(receivedBytes, "EUC-KR");

            if (receivedMessage.isEmpty() || receivedMessage.equals("NULL")) {
                log.info("NO DATA RECEIVED FROM VAN\r\n");
                return "F";
            }

            log.info("[RFC] VAN -> DEMON: [{}] [{}byte] [{}]", importParam1, receivedBytes.length, receivedMessage);
            setSendToSap(receivedMessage, importParam1);

            long endTime = System.currentTimeMillis() - startTime;
            log.debug("[SUCCESS] UPLOAD ({}sec)", endTime * 0.001);
            return "S";
        } catch (IOException e) {
            log.error("DATA VALUE: {}\r\n", e.getMessage());
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

                long startTime = System.currentTimeMillis();
                byte[] bytes = new byte[300];
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                dataInputStream.readFully(bytes);
                String receiveMessage = new String(bytes, "EUC-KR");
                if (receiveMessage.contains("6000200") || receiveMessage.contains("4000500") || receiveMessage.contains("4000501")) {
                    log.info("[RECEIVED BILL DATA] VAN -> DEMON [{}byte] [{}] ({}sec)", receiveMessage.getBytes().length, receiveMessage, (System.currentTimeMillis() - startTime) * 0.001);
                    try {
                        JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                        JCoFunction jCoFunctionBILL = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.BILL"));
                        jCoFunctionBILL.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.BILL"), receiveMessage);
                        jCoFunctionBILL.execute(jCoDestination);
                        log.info("[RECEIVE BILL DATA] DEMON -> SAP [{}]", jCoFunctionBILL.getName());

                        StringBuilder stringBuilder = new StringBuilder(receiveMessage);
                        stringBuilder.setCharAt(24, '1');
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataOutputStream.write(stringBuilder.toString().getBytes());
                        dataOutputStream.flush();
                        log.info("[RECEIVED BILL DATA] DEMON -> VAN [{}]\r\n", stringBuilder);
                    } catch (JCoException e) {
                        log.error("ERROR BILL SERVER SOCKET: {}", e.getMessage());
                    }
                } else if (receiveMessage.isEmpty() || receiveMessage.equals("null")) {
                    log.info("RECEIVED DATA IS NULL");
                } else {
                    log.info("[RECEIVED KRW DATA] VAN -> DEMON [{}byte] [{}] ({}sec)", receiveMessage.getBytes().length, receiveMessage, (System.currentTimeMillis() - startTime) * 0.001);
                    try {
                        JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                        JCoFunction jCoFunctionKRW = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.KRW"));
                        jCoFunctionKRW.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.KRW"), receiveMessage);
                        jCoFunctionKRW.execute(jCoDestination);
                        log.info("[RECEIVE KRW DATA] DEMON -> SAP [{}]", jCoFunctionKRW.getName());

                        StringBuilder stringBuilder = new StringBuilder(receiveMessage);
                        stringBuilder.setCharAt(21, '1');
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataOutputStream.write(stringBuilder.toString().getBytes());
                        dataOutputStream.flush();
                        log.info("[RECEIVED KRW DATA] DEMON -> VAN [{}]\r\n", stringBuilder);
                    } catch (JCoException e) {
                        log.error("ERROR KRW SERVER SOCKET: {}", e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("RECEIVED DATA VALUE [{}]", e.getMessage());
        }
    }


    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(properties.getProperty("SOCKET.PORT.KEB")));
            while (true) {
                socket = serverSocket.accept();

                long startTime = System.currentTimeMillis();
                byte[] bytes = new byte[2000];
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                dataInputStream.readFully(bytes);
                String receiveMessage = new String(bytes, "EUC-KR");
                log.info("[RECEIVED KEB DATA] VAN -> DEMON [{}byte] [{}] ({}sec)\r\n", receiveMessage.getBytes().length, receiveMessage, (System.currentTimeMillis() - startTime) * 0.001);
                try {
                    JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                    JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.KEB"));
                    jCoFunction.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.KEB"), receiveMessage);
                    jCoFunction.execute(jCoDestination);
                    log.info("[RECEIVE KEB DATA] DEMON -> SAP [{}]", jCoFunction.getName());

                    StringBuilder stringBuilder = new StringBuilder(receiveMessage);
                    stringBuilder.setCharAt(24, '1');
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.write(stringBuilder.toString().getBytes());
                    dataOutputStream.flush();
                    log.info("[RECEIVED KEB DATA] DEMON -> VAN [{}]", stringBuilder);
                } catch (JCoException e) {
                    log.error("JCO DESTINATION ERROR: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("RECEIVED KEB DATA VALUE [{}]", e.getMessage());
        }
    }

//    private byte[] truncateToBytes(String value, int len) throws UnsupportedEncodingException {
//        byte[] bytes = value.getBytes("EUC-KR");
//
//        if (bytes.length <= len) {
//            return value.getBytes();
//        }
//
//        int truncatedLength = len;
//        while (true) {
//            if ((bytes[truncatedLength] & 0xC0) != 0x80) {
//                break;
//            }
//            truncatedLength--;
//        }
//
//        byte[] truncatedBytes = new byte[truncatedLength];
//        System.arraycopy(bytes, 0, truncatedBytes, 0, truncatedLength);
//
//        return truncatedBytes;
//    }

    private void setSendToSap(String value, String type) {
        long startTime = System.currentTimeMillis();
        switch (type) {
            case "WON":
                try {
                    JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                    JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.KRW"));
                    jCoFunction.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.KRW"), value);
                    jCoFunction.execute(jCoDestination);
                    log.info("[RFC] DEMON(WON) -> SAP ({}sec)\r\n", (System.currentTimeMillis() - startTime) * 0.001);
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
                    log.info("[RFC] DEMON(KEB) -> SAP ({}sec)\r\n", (System.currentTimeMillis() - startTime) * 0.001);
                } catch (JCoException e) {
                    log.error("DEMON(KEB) -> SAP: {}", e.getMessage());
                }
                break;
            case "BILL":
                try {
                    JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));
                    JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("JCO.FUNCTION.BILL"));
                    jCoFunction.getImportParameterList().setValue(properties.getProperty("JCO.PARAM.IMPORT0.BILL"), value);
                    jCoFunction.execute(jCoDestination);
                    log.info("[RFC] DEMON(BILL) -> SAP ({}sec)\r\n", (System.currentTimeMillis() - startTime) * 0.001);
                } catch (JCoException e) {
                    log.error("DEMON(BILL) -> SAP: {}", e.getMessage());
                }
                break;
        }
    }
}