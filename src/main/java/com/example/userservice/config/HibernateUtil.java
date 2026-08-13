package com.example.userservice.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);

    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {

    }

    private static SessionFactory buildSessionFactory() {
        try {
            log.info("Инициализация Hibernate SessionFactory...");
            SessionFactory factory = new Configuration().configure().buildSessionFactory();
            log.info("SessionFactory успешно создана.");
            return factory;
        } catch (Throwable ex) {
            log.error("Не удалось инициализировать SessionFactory", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        log.info("Закрытие SessionFactory...");
        getSessionFactory().close();
    }
}
