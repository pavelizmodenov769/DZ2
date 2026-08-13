package com.example.userservice.dao;

import com.example.userservice.config.HibernateUtil;
import com.example.userservice.exception.DAOException;
import com.example.userservice.model.User;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);

    @Override
    public User create(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            log.info("Создан пользователь id={}, email={}", user.getId(), user.getEmail());
            return user;
        } catch (Exception e) {
            rollbackQuietly(tx);
            log.error("Не удалось создать пользователя", e);
            throw new DAOException("Не удалось создать пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        } catch (Exception e) {
            log.error("Не удалось получить пользователя id={}", id, e);
            throw new DAOException("Не удалось получить пользователя id=" + id, e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User ORDER BY id", User.class).list();
        } catch (Exception e) {
            log.error("Не удалось получить список пользователей", e);
            throw new DAOException("Не удалось получить список пользователей", e);
        }
    }

    @Override
    public User update(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User merged = session.merge(user);
            tx.commit();
            log.info("Обновлен пользователь id={}", user.getId());
            return merged;
        } catch (Exception e) {
            rollbackQuietly(tx);
            log.error("не удалось обновить пользователя id={}", user.getId(), e);
            throw new DAOException("Не удалось обновить пользователя id=" + user.getId(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
            }
            tx.commit();
            log.info("Удален пользователь id={}{}", id, user == null ? "  (не найден)" : "");
        } catch (Exception e) {
            rollbackQuietly(tx);
            log.error("Не удалось удалить пользователя id={}", id, e);
            throw new DAOException("Не удалось удалить пользователя с id=" + id, e);
        }
    }

    private void rollbackQuietly(Transaction tx) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
            } catch (Exception rbEx) {
                log.warn("Ошибка при откате транзакций", rbEx);
            }
        }
    }
}