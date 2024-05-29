package suhun.kim.jco;

import suhun.kim.util.PropertiesUtil;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class JCoServerConfig {
    private static final Logger log = LoggerFactory.getLogger(JCoServerConfig.class);
    private final Properties properties = PropertiesUtil.getProperties();

    public JCoServerConfig() {
        long startTime = System.currentTimeMillis();
        if (properties.getProperty("JCO.TYPE").equals("MESSAGE")) {
            log.info("MESSAGE SERVER");
            properties.setProperty(DestinationDataProvider.JCO_MSHOST, properties.getProperty("JCO.CLIENT.MSHOST"));
            properties.setProperty(DestinationDataProvider.JCO_MSSERV, properties.getProperty("JCO.CLIENT.MSSERV"));
            properties.setProperty(DestinationDataProvider.JCO_R3NAME, properties.getProperty("JCO.CLIENT.R3NAME"));
            properties.setProperty(DestinationDataProvider.JCO_GROUP, properties.getProperty("JCO.CLIENT.GROUP"));

            properties.setProperty(DestinationDataProvider.JCO_ASHOST, properties.getProperty("JCO.CLIENT.ASHOST"));
            properties.setProperty(DestinationDataProvider.JCO_SYSNR, properties.getProperty("JCO.CLIENT.SYSNR"));
            properties.setProperty(DestinationDataProvider.JCO_CLIENT, properties.getProperty("JCO.CLIENT.CLIENT"));
            properties.setProperty(DestinationDataProvider.JCO_USER, properties.getProperty("JCO.CLIENT.USER"));
            properties.setProperty(DestinationDataProvider.JCO_PASSWD, properties.getProperty("JCO.CLIENT.PASSWD"));
            properties.setProperty(DestinationDataProvider.JCO_LANG, properties.getProperty("JCO.CLIENT.LANG"));
            properties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, properties.getProperty("JCO.DESTINATION.POOL_CAPACITY"));
            properties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, properties.getProperty("JCO.DESTINATION.PEAK_LIMIT"));

            properties.setProperty(ServerDataProvider.JCO_GWHOST, properties.getProperty("JCO.SERVER.GWHOST"));
            properties.setProperty(ServerDataProvider.JCO_GWSERV, properties.getProperty("JCO.SERVER.GWSERV"));
            properties.setProperty(ServerDataProvider.JCO_PROGID, properties.getProperty("JCO.SERVER.PROGID"));
            properties.setProperty(ServerDataProvider.JCO_CONNECTION_COUNT, properties.getProperty("JCO.SERVER.CONNECTION_COUNT"));
            properties.setProperty(ServerDataProvider.JCO_REP_DEST, properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));

            createDataFile(properties.getProperty("JCO.SERVER.PROGID"), properties, ".JcoServer");
            createDataFile(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"), properties, ".JcoDestination");
        } else {
            properties.setProperty(DestinationDataProvider.JCO_ASHOST, properties.getProperty("JCO.CLIENT.ASHOST"));
            properties.setProperty(DestinationDataProvider.JCO_SYSNR, properties.getProperty("JCO.CLIENT.SYSNR"));
            properties.setProperty(DestinationDataProvider.JCO_CLIENT, properties.getProperty("JCO.CLIENT.CLIENT"));
            properties.setProperty(DestinationDataProvider.JCO_USER, properties.getProperty("JCO.CLIENT.USER"));
            properties.setProperty(DestinationDataProvider.JCO_PASSWD, properties.getProperty("JCO.CLIENT.PASSWD"));
            properties.setProperty(DestinationDataProvider.JCO_LANG, properties.getProperty("JCO.CLIENT.LANG"));
            properties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, properties.getProperty("JCO.DESTINATION.POOL_CAPACITY"));
            properties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, properties.getProperty("JCO.DESTINATION.PEAK_LIMIT"));

            properties.setProperty(ServerDataProvider.JCO_GWHOST, properties.getProperty("JCO.SERVER.GWHOST"));
            properties.setProperty(ServerDataProvider.JCO_GWSERV, properties.getProperty("JCO.SERVER.GWSERV"));
            properties.setProperty(ServerDataProvider.JCO_PROGID, properties.getProperty("JCO.SERVER.PROGID"));
            properties.setProperty(ServerDataProvider.JCO_CONNECTION_COUNT, properties.getProperty("JCO.SERVER.CONNECTION_COUNT"));
            properties.setProperty(ServerDataProvider.JCO_REP_DEST, properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"));

            createDataFile(properties.getProperty("JCO.SERVER.PROGID"), properties, ".JcoServer");
            createDataFile(properties.getProperty("JCO.SERVER.REPOSITORY_DESTINATION"), properties, ".JcoDestination");
        }

        try {
            JCoServer jCoServer = JCoServerFactory.getServer(properties.getProperty("JCO.SERVER.PROGID"));
            jCoServer.setCallHandlerFactory(new JCoServerFunctionHandlerFactoryImpl());
            jCoServer.addServerExceptionListener(new JCoServerListenerImpl());
            jCoServer.addServerErrorListener(new JCoServerListenerImpl());
            jCoServer.addServerStateChangedListener(new JCoServerStateChangedListenerImpl());
            jCoServer.setTIDHandler(new JCoServerTIDHandlerImpl());
            jCoServer.start();

            long result = System.currentTimeMillis() - startTime;
            log.info("[JCO CONFIG SUCCESS] {} ({}sec)\r\n", jCoServer.getProgramID(), result * 0.001);
        } catch (JCoException e) {
            log.info("JCO CONFIG ERROR: {}", e.getMessage());
        }
    }

    private void createDataFile(String destinationName, Properties properties, String string) {
        File file = new File(destinationName + string);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            properties.store(fileOutputStream, "For test purposes only");
            fileOutputStream.close();
        } catch (IOException e) {
            log.info("JCO CREATE DATA FILES ERROR: {}", e.getMessage());
        }
    }
}