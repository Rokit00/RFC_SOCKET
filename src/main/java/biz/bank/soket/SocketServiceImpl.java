package biz.bank.soket;

import biz.bank.util.Crypt;
import biz.bank.util.PropertiesUtil;
import biz.bank.util.TripleDESImpl;
import com.sap.conn.jco.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class SocketServiceImpl implements SocketService {
    private static final Logger log = LoggerFactory.getLogger(SocketServiceImpl.class);
    private static final Properties properties = PropertiesUtil.getProperties();
    private final Crypt crypt = new TripleDESImpl();
    private Socket socket;

    @Override
    public void setKRWSocket() {
        try {
            socket = new Socket(properties.getProperty("SOCKET.IP"), Integer.parseInt(properties.getProperty("SOCKET.PORT.KRW")));
            log.info("CONNECTED TO VAN {}:{}", properties.getProperty("SOCKET.IP"), properties.getProperty("SOCKET.PORT.KRW"));
        } catch (IOException e) {
            log.error("FAILED TO CONNECT TO VAN: {}", e.getMessage());
        }
    }

    @Override
    public void setKEBSocket() {
        try {
            socket = new Socket(properties.getProperty("SOCKET.IP"), Integer.parseInt(properties.getProperty("SOCKET.PORT.KEB")));
            log.info("CONNECTED TO VAN {}:{}", properties.getProperty("SOCKET.IP"), properties.getProperty("SOCKET.PORT.KEB"));
        } catch (IOException e) {
            log.error("FAILED TO CONNECT TO VAN: {}", e.getMessage());
        }
    }

    @Override
    public void disConnect() {
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
            if (importParam1.equals("WON")) {
                setKRWSocket();
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                byte[] toBytes = truncateToBytes(importParam, 300);
                log.info("CONTENT: [{}byte] [{}]", toBytes.length, importParam);
                log.info("TYPE: {}", importParam1);

                outputStream.write(toBytes);
                outputStream.flush();

                log.info("DATA SENT TO VAN");

                DataInputStream reader = new DataInputStream(socket.getInputStream());

                byte[] receivedBytes = new byte[300];
                reader.readFully(receivedBytes);
                String receivedMessage = new String(receivedBytes, "EUC-KR");

                if (receivedMessage.isEmpty()) {
                    log.info("NO DATA RECEIVED FROM VAN");
                    return "F";
                }

                log.info("DATA RECEIVED FROM VAN: [{}byte] [{}]", receivedBytes.length, receivedMessage);
                setSendToSap(receivedMessage, receivedMessage);

                long endTime = System.currentTimeMillis() - startTime;
                log.info("[SUCCESS] SAP -> VAN -> SAP({}sec)", endTime * 0.001);

                return "S";
            } else if (importParam1.equals("KEB")) {
                setKEBSocket();
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                byte[] toBytes = truncateToBytes(importParam, 2000);
                log.info("DATA: [{}byte] [{}]", toBytes.length, importParam);
                log.info("TYPE: {}", importParam1);

                dataOutputStream.write(toBytes);
                dataOutputStream.flush();

                log.info("DATA SENT TO VAN");

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                byte[] receivedBytes = new byte[2000];
                dataInputStream.readFully(receivedBytes);
                String receivedMessage = new String(receivedBytes, "EUC-KR");

                if (receivedMessage.isEmpty()) {
                    log.info("NO DATA RECEIVED FROM VAN");
                    return "F";
                }

                log.info("DATA RECEIVED FROM VAN: [{}byte] [{}]", receivedBytes.length, receivedMessage);
                setSendToSap(receivedMessage, receivedMessage);

                long endTime = System.currentTimeMillis() - startTime;
                log.info("[SUCCESS] SAP -> VAN -> SAP({}sec)", endTime * 0.001);

                return "S";
            } else {
                log.info("TYPE IS NOT KRW OR KEB");
                return "F";
            }
        } catch (IOException e) {
            log.error("SEND FAILED: {}", e.getMessage());
            return "F";
        } finally {
            disConnect();
        }
    }

    private byte[] truncateToBytes(String input, int len) throws UnsupportedEncodingException {
        byte[] bytes = input.getBytes("UTF-8");

        if (bytes.length <= len) {
            return input.getBytes();
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
        if (type.getBytes().length == 300) {
            try {
                JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("jco.server.repository_destination"));
                JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("jco.function.krw"));
                jCoFunction.getImportParameterList().setValue(properties.getProperty("jco.param.import0.krw"), value);
                jCoFunction.execute(jCoDestination);
            } catch (JCoException e) {
                log.error("ERROR KRW: {}", e.getMessage());
            }
        } else {
            try {
                JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("jco.server.repository_destination"));
                JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("jco.function.keb"));
                jCoFunction.getImportParameterList().setValue(properties.getProperty("jco.param.import0.keb"), value);
                jCoFunction.execute(jCoDestination);
            } catch (JCoException e) {
                log.error("ERROR KEB: {}", e.getMessage());
            }
        }
    }
}