package org.jtc.common.api.factory;

import javax.ejb.EJBHome;

/**
 * $LastChangedBy: cgoina $:
 * $LastChangedDate: 2010-03-12 11:47:23 -0500 (Fri, 12 Mar 2010) $:
 * $Revision: 30631 $:
 * $HeadURL: http://svn.jcvi.org/SE/LIMS/trunk/common/ejb/main/src/java/org/jtc/common/api/factory/HomeInterfaceCacheNode.java $:
 * @deprecated
 */
class HomeInterfaceCacheNode {

    private EJBHome home;
    private long retrievalTime;

    HomeInterfaceCacheNode(EJBHome home, long retrievalTime) {
        this.home = home;
        this.retrievalTime = retrievalTime;
    }

    EJBHome getHome() {
        return home;
    }

    long getRetrievalTime() {
        return retrievalTime;
    }
}
