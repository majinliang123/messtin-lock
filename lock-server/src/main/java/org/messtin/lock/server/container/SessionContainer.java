package org.messtin.lock.server.container;

import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.server.entity.Session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The container of session.
 * When user connect to server, it will register a session here.
 *
 * @author majinliang
 */
public final class SessionContainer {
    private static final Logger logger = LogManager.getLogger(SessionContainer.class);

    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * When the session is existed at {@link #sessionMap}, will avoid user to register it again.
     * and will return false to tell user the processed already registered.
     *
     * @param session the session we will register.
     * @return if we register success.
     */
    public static boolean register(Session session) {
        String sessionId = session.getSessionId();
        logger.info("Try to add session {} into sessionMap.", sessionId);
        if (sessionMap.containsKey(sessionId)) {
            logger.warn("{} already registered.", sessionId);
            return false;
        }
        sessionMap.put(sessionId, session);
        logger.info("Added {} into sesssionMap.", sessionId);
        return true;
    }

    /**
     * When the session does not exist at {@link #sessionMap},
     * will return user false to tell user we could not remove it.
     *
     * @param sessionId the sessionId we want to remove.
     * @return if we remove success.
     */
    public static boolean deregister(String sessionId) {
        logger.info("Try to remove session {} from sessionMap.", sessionId);
        if (!sessionMap.containsKey(sessionId)) {
            logger.warn("{} not existed at sessionMap.", sessionId);
            return false;
        }
        sessionMap.remove(sessionId);
        logger.info("Removed {} from sesssionMap.", sessionId);
        return true;
    }

    public static Session get(Channel channel) {
        Optional<Session> session = sessionMap.values()
                .stream()
                .filter(s -> s.getChannel().equals(channel))
                .findFirst();
        return session.get();
    }

    /**
     * Check if sessionId already connected.
     *
     * @param sessionId the session id you want to check.
     * @return
     */
    public static boolean isExist(String sessionId) {
        return sessionMap.containsKey(sessionId);
    }
}
