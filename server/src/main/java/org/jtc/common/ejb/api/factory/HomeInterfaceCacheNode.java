package org.jtc.common.ejb.api.factory;

import javax.ejb.EJBHome;

public class HomeInterfaceCacheNode {

    private EJBHome home;
    private long retrievalTime;

    public HomeInterfaceCacheNode(EJBHome home, long retrievalTime) {
        this.home = home;
        this.retrievalTime = retrievalTime;
    }

    public EJBHome getHome() {
        return home;
    }

    public long getRetrievalTime() {
        return retrievalTime;
    }
}
