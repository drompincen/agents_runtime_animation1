package io.github.drompincen.archviz;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.mockito.Mockito.*;

class HtmlAccessLogFilterTest {

    private final HtmlAccessLogFilter filter = new HtmlAccessLogFilter();

    @Test
    void doFilter_htmlRequest_chainsAndLogs() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/collab-animation.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_nonHtmlRequest_chainsWithoutIssue() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/diagrams");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_usesXForwardedFor() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test.html");
        request.addHeader("X-Forwarded-For", "10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_fallsBackToRemoteAddr() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test.html");
        // No X-Forwarded-For header set
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
