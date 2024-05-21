package biz.bank.jco;

import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCoServerListenerImpl implements JCoServerErrorListener, JCoServerExceptionListener {
    private static final Logger log = LoggerFactory.getLogger(JCoServerListenerImpl.class);

    @Override
    public void serverErrorOccurred(JCoServer jcoServer, String connectionId, JCoServerContextInfo jCoServerContextInfo, Error error) {
        log.error("[SERVER ERROR OCCURRED] {}", error.getMessage());
        jcoServer.stop();
    }
    @Override
    public void serverExceptionOccurred(JCoServer jcoServer, String connectionId, JCoServerContextInfo jCoServerContextInfo, Exception exception) {
        log.error("[SERVER EXCEPTION OCCURRED] {}", exception.getMessage());
        jcoServer.stop();
    }
}
