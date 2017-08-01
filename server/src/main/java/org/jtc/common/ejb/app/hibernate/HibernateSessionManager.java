package org.jtc.common.ejb.app.hibernate;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jtc.common.ejb.api.exception.SystemErrorException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.HashMap;

public class HibernateSessionManager {

    private static final HashMap<String,HibernateSessionManager> sessionMgrTable = new HashMap<String,HibernateSessionManager>();

    static public HibernateSessionManager getInstance(String sessionName) {
        HibernateSessionManager sessionMgr = sessionMgrTable.get(sessionName);
            
        if(sessionMgr == null) {
            sessionMgr = new HibernateSessionManager(sessionName);
            sessionMgrTable.put(sessionName,sessionMgr);
        }
        return sessionMgr;
    }

    private String sessionName;
    private SessionFactory sessionFactory;

    private HibernateSessionManager(String sessionName) {
        this.sessionName = sessionName;
    }

    public SessionFactory getSessionFactory() throws SystemErrorException {
        if(sessionFactory == null) {
            // -- instantiate the session factory
            try {
                EntityManagerFactory em = (EntityManagerFactory) new InitialContext().lookup(sessionName + "Factory");
                sessionFactory = em.unwrap(SessionFactory.class);

            } catch(NamingException e) {
                throw new SystemErrorException(e);
            }
        }
        return sessionFactory;
    }


    public Session getSession() throws SystemErrorException {
        Session session;
        try {
            EntityManager em = (EntityManager) new InitialContext().lookup(sessionName);
            session = em.unwrap(Session.class);

            if(!session.isOpen()){
                session = session.getSessionFactory().openSession();
            }
        } catch(NamingException e) {
            throw new SystemErrorException(e);
        }
        return session;
    }

    public Session getCurrentSession() throws SystemErrorException {
        Session currentSession = getSession();
        currentSession.setFlushMode(FlushMode.MANUAL);
        return currentSession;
    }

    public Session openSession() throws SystemErrorException {
        Session newSession = getSession();
        return newSession;
    }

    public void closeSession(Session session) {
        if(session != null && session.isOpen()) {
            session.flush();
            session.clear();
            session.close();
        }
    }

}
