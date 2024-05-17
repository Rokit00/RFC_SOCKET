package biz.bank.jco;

import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerStateChangedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCoServerStateChangedListenerImpl implements JCoServerStateChangedListener {
    private static final Logger log = LoggerFactory.getLogger(JCoServerStateChangedListenerImpl.class);
    @Override
    public void serverStateChangeOccurred(JCoServer jCoServer, JCoServerState jCoServerState, JCoServerState newState) {
        log.info("JCO SERVER STATE: [{}] [{}]\r\n", newState.name(), jCoServer.getProgramID());
    }
}
