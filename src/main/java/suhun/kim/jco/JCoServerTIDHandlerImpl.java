package suhun.kim.jco;

import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerTIDHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCoServerTIDHandlerImpl implements JCoServerTIDHandler {
    private static final Logger log = LoggerFactory.getLogger(JCoServerTIDHandlerImpl.class);

    @Override
    public boolean checkTID(JCoServerContext jCoServerContext, String s) {
        log.info(s);
        return false;
    }

    @Override
    public void confirmTID(JCoServerContext jCoServerContext, String s) {
        log.info(s);
    }

    @Override
    public void commit(JCoServerContext jCoServerContext, String s) {
        log.info(s);
    }

    @Override
    public void rollback(JCoServerContext jCoServerContext, String s) {
        log.info(s);
    }
}

