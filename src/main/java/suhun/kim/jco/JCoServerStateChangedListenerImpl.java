package suhun.kim.jco;

import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerStateChangedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suhun.kim.util.LogAPIUtil;

public class JCoServerStateChangedListenerImpl implements JCoServerStateChangedListener {
    private final LogAPIUtil logAPIUtil = new LogAPIUtil();
    private static final Logger log = LoggerFactory.getLogger(JCoServerStateChangedListenerImpl.class);

    @Override
    public void serverStateChangeOccurred(JCoServer jCoServer, JCoServerState jCoServerState, JCoServerState newState) {
        logAPIUtil.send(newState.name());
        log.debug("JCO SERVER STATE: [{}] [{}]\r\n", newState.name(), jCoServer.getProgramID());
    }
}
