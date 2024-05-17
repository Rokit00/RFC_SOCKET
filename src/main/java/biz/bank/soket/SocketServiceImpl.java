package biz.bank.soket;

import biz.bank.util.Crypt;
import biz.bank.util.PropertiesUtil;
import biz.bank.util.TripleDESImpl;
import com.sap.conn.jco.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class SocketServiceImpl implements SocketService {
    private static final Logger log = LoggerFactory.getLogger(SocketServiceImpl.class);
    private static final Properties properties = PropertiesUtil.getProperties();
    private final Crypt crypt = new TripleDESImpl();
    private Socket socket;

    @Override
    public void setSocket() {
        try {
            socket = new Socket(properties.getProperty("socket.ip"), Integer.parseInt(properties.getProperty("socket.port")));
            log.info("Connected to server {}:{}", properties.getProperty("socket.ip"), properties.getProperty("socket.port"));
        } catch (IOException e) {
            log.error("Failed to connect to server: {}", e.getMessage());
        }
    }

    @Override
    public void disConnect() {
        try {
            if (socket != null) {
                socket.close();
                log.info("Disconnected from server");
            }
        } catch (IOException e) {
            log.error("Failed to disconnect from server: {}", e.getMessage());
        }
    }

    @Override
    public String logic(String importParam, String importParam1) {
        long startTime = System.currentTimeMillis();
        setSocket();
        try {
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            byte[] a = truncateToBytes(importParam, 300);
            log.info("DATA: {} {}", a.length, importParam);
            log.info(importParam1);

            outputStream.write(a);
            outputStream.flush();

            log.info("Data sent to server");

            DataInputStream reader = new DataInputStream(socket.getInputStream());

            StringBuilder messageBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                messageBuilder.append(line);
            }
            String receivedMessage = messageBuilder.toString();
            log.info("Received message from server: {}", receivedMessage);

            if (receivedMessage.isEmpty()) {
                log.info("No data received from server");
                return "F";
            }

            log.info("KRW [{}] [{}]", receivedMessage.length(), receivedMessage);
            setKRW(receivedMessage);

            long endTime = System.currentTimeMillis() - startTime;
            log.info("[SOCKET SEND SUCCESS] SAP -> VAN ({}sec)", endTime * 0.001);

            return "S";
        } catch (IOException e) {
            log.error("SOCKET SEND FAILED: {}", e.getMessage());
            return "F";
        }
    }

    private static byte[] truncateToBytes(String input, int len) throws UnsupportedEncodingException {
        byte[] utf8Bytes = input.getBytes("UTF-8");

        if (utf8Bytes.length <= len) {
            return input.getBytes();
        }

        int truncatedLength = len;
        while (true) {
            if ((utf8Bytes[truncatedLength] & 0xC0) != 0x80) {
                break;
            }
            truncatedLength--;
        }

        byte[] truncatedBytes = new byte[truncatedLength];
        System.arraycopy(utf8Bytes, 0, truncatedBytes, 0, truncatedLength);

        return truncatedBytes;
    }


    private void setKRW(String value) {
        try {
            JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("jco.server.repository_destination"));
            JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("jco.function.krw"));
            jCoFunction.getImportParameterList().setValue(properties.getProperty("jco.param.import0.krw"), value);
            jCoFunction.execute(jCoDestination);
        } catch (JCoException e) {
            log.error("Error setting KRW: {}", e.getMessage());
        }
    }

    private void setKEB(String value) {
        try {
            JCoDestination jCoDestination = JCoDestinationManager.getDestination(properties.getProperty("jco.server.repository_destination"));
            JCoFunction jCoFunction = jCoDestination.getRepository().getFunction(properties.getProperty("jco.function.keb"));
            jCoFunction.getImportParameterList().setValue(properties.getProperty("jco.param.import0.keb"), value);
            jCoFunction.execute(jCoDestination);
        } catch (JCoException e) {
            log.error("Error setting KEB: {}", e.getMessage());
        }
    }
}
