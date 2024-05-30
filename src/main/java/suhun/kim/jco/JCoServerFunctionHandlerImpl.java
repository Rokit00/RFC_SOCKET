package suhun.kim.jco;

import suhun.kim.socket.SocketService;
import suhun.kim.socket.SocketServiceImpl;
import suhun.kim.util.PropertiesUtil;
import com.sap.conn.jco.*;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class JCoServerFunctionHandlerImpl implements JCoServerFunctionHandler {
    private static final Logger log = LoggerFactory.getLogger(JCoServerFunctionHandlerImpl.class);
    private final SocketService socketService = new SocketServiceImpl();
    private final Properties properties = PropertiesUtil.getProperties();

    @Override
    public void handleRequest(JCoServerContext jCoServerContext, JCoFunction jCoFunction) throws AbapException, AbapClassException {
        long startTime = System.currentTimeMillis();
        log.debug("[START] RFC HANDLE REQUEST");

        JCoParameterList importParams = jCoFunction.getImportParameterList();
        JCoParameterList exportParams = jCoFunction.getExportParameterList();

        String importParam0 = importParams.getString(properties.getProperty("JCO.PARAM.IMPORT0"));
        String importParam1 = importParams.getString(properties.getProperty("JCO.PARAM.IMPORT1"));

        String result = socketService.logic(importParam0, importParam1);

        exportParams.setValue(properties.getProperty("JCO.PARAM.EXPORT"), result);

        long resultTime = System.currentTimeMillis() - startTime;
        log.debug("[END] RFC HANDLE REQUEST ({}sec) \r\n", resultTime * 0.001);
    }
}