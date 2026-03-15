package com.fablix.servlet;

import com.fablix.util.SearchTimingLogger;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@WebFilter("/movie-list")
public class SearchTimingFilter implements Filter {
    public static final String JDBC_TIME_ATTRIBUTE = "searchTiming.jdbcExecutionTimeNs";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        long servletStartTime = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            Object jdbcTime = request.getAttribute(JDBC_TIME_ATTRIBUTE);
            if (jdbcTime instanceof Long) {
                long servletEndTime = System.nanoTime();
                long servletElapsedTime = servletEndTime - servletStartTime;
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                SearchTimingLogger.logSample(
                        httpRequest.getServletContext(),
                        servletElapsedTime,
                        (Long) jdbcTime
                );
            }
        }
    }
}
