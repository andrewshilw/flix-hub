package com.fablix.util;

import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public final class RedisSessionManager {
    private static final String COOKIE_NAME = "redisSessionId";
    private static final String SESSION_PREFIX = "session:";
    private static final String REQUEST_SESSION_KEY = RedisSessionManager.class.getName() + ".session";
    private static final String REQUEST_SESSION_ID_KEY = RedisSessionManager.class.getName() + ".sessionId";
    private static final int SESSION_TTL_SECONDS = 24 * 60 * 60;
    private static final Gson GSON = new Gson();

    private RedisSessionManager() {}

    public static void init() {
        RedisUtil.init();
    }

    public static RedisSession loadSession(HttpServletRequest request) {
        Object cached = request.getAttribute(REQUEST_SESSION_KEY);
        if (cached instanceof RedisSession) {
            return (RedisSession) cached;
        }

        String sessionId = getSessionId(request);
        if (sessionId == null) {
            return null;
        }

        String sessionJson = RedisUtil.get(SESSION_PREFIX + sessionId);
        if (sessionJson == null || sessionJson.isBlank()) {
            return null;
        }

        RedisSession session = GSON.fromJson(sessionJson, RedisSession.class);
        if (session == null) {
            session = new RedisSession();
        }

        cache(request, sessionId, session);
        return session;
    }

    public static RedisSession getOrCreateSession(HttpServletRequest request, HttpServletResponse response) {
        RedisSession session = loadSession(request);
        if (session != null) {
            return session;
        }

        String sessionId = UUID.randomUUID().toString();
        session = new RedisSession();
        cache(request, sessionId, session);
        writeCookie(response, sessionId, SESSION_TTL_SECONDS);
        return session;
    }

    public static void saveSession(HttpServletRequest request, HttpServletResponse response, RedisSession session) {
        String sessionId = (String) request.getAttribute(REQUEST_SESSION_ID_KEY);
        if (sessionId == null) {
            sessionId = getSessionId(request);
        }
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }

        cache(request, sessionId, session);
        RedisUtil.set(SESSION_PREFIX + sessionId, GSON.toJson(session), SESSION_TTL_SECONDS);
        writeCookie(response, sessionId, SESSION_TTL_SECONDS);
    }

    public static void destroySession(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = (String) request.getAttribute(REQUEST_SESSION_ID_KEY);
        if (sessionId == null) {
            sessionId = getSessionId(request);
        }
        if (sessionId != null) {
            RedisUtil.delete(SESSION_PREFIX + sessionId);
        }

        request.removeAttribute(REQUEST_SESSION_KEY);
        request.removeAttribute(REQUEST_SESSION_ID_KEY);
        writeCookie(response, "", 0);
    }

    private static void cache(HttpServletRequest request, String sessionId, RedisSession session) {
        request.setAttribute(REQUEST_SESSION_KEY, session);
        request.setAttribute(REQUEST_SESSION_ID_KEY, sessionId);
    }

    private static String getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static void writeCookie(HttpServletResponse response, String value, int maxAge) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
