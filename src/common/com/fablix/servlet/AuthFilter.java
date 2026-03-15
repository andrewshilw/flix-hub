package com.fablix.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fablix.util.RedisSession;
import com.fablix.util.RedisSessionManager;

import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        RedisSessionManager.init();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String path = httpRequest.getRequestURI().substring(contextPath.length());

        if (path.startsWith("/_dashboard")) {
            chain.doFilter(request, response);
            return;
        }

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        RedisSession session = RedisSessionManager.loadSession(httpRequest);
        boolean loggedIn = session != null && session.getCustomerEmail() != null;

        if (!loggedIn) {
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        httpRequest.setAttribute("customerEmail", session.getCustomerEmail());
        httpRequest.setAttribute("customerId", session.getCustomerId());
        httpRequest.setAttribute("customerLoginTime", session.getCustomerLoginTime());
        RedisSessionManager.saveSession(httpRequest, httpResponse, session);
        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        if (path.equals("/login") || path.equals("/login.jsp")) {
            return true;
        }
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".gif")
                || path.endsWith(".ico");
    }
}
