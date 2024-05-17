package biz.bank.jco;

import biz.bank.soket.SocketService;
import biz.bank.soket.SocketServiceImpl;
import biz.bank.util.PropertiesUtil;
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

        JCoParameterList importParams = jCoFunction.getImportParameterList();
        JCoParameterList exportParams = jCoFunction.getExportParameterList();

        String importParam0 = importParams.getString(properties.getProperty("jco.param.import0"));
        String importParam1 = importParams.getString(properties.getProperty("jco.param.import1"));

        socketService.setSocket();
        String result = socketService.logic(importParam0, importParam1);

        exportParams.setValue(properties.getProperty("jco.param.export"), result);

        long resultTime = System.currentTimeMillis() - startTime;
        log.info("RFC HANDLE REQUEST SUCCESS ({}sec)\r\n", resultTime * 0.001);
    }
}
