package biz.bank.jco;

import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import com.sap.conn.jco.server.JCoServerFunctionHandlerFactory;

public class JCoServerFunctionHandlerFactoryImpl implements JCoServerFunctionHandlerFactory {
    @Override
    public JCoServerFunctionHandler getCallHandler(JCoServerContext jCoServerContext, String s) {
        return new JCoServerFunctionHandlerImpl();
    }

    @Override
    public void sessionClosed(JCoServerContext jCoServerContext, String s, boolean b) {

    }
}
