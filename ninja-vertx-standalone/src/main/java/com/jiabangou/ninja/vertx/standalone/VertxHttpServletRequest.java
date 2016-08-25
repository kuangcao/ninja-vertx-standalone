package com.jiabangou.ninja.vertx.standalone;

import com.jiabangou.ninja.vertx.standalone.utils.DateParser;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.security.Principal;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * VertxHttpServletRequest
 * Created by freeway on 16/8/18.
 */
public class VertxHttpServletRequest implements HttpServletRequest {

    private HttpServerRequest httpServerRequest;
    private RoutingContext event;
    private String serverName;
    private int serverPort;
    private String remoteAddress;

    private String contextPath;

    public VertxHttpServletRequest(RoutingContext event) {
        this.httpServerRequest = event.request();
        this.event = event;
        String fullUrl = this.httpServerRequest.absoluteURI();
        URI uri;
        try {
            uri = new URI(fullUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InvalidParameterException("Request to invalid URL " + fullUrl + " while constructing VertxWebContext");
        }
        this.serverName = uri.getHost();
        this.serverPort = (uri.getPort() != -1) ? uri.getPort() : httpServerRequest.scheme().equals("http") ? 80 : 443;
        this.remoteAddress = httpServerRequest.remoteAddress().toString();

    }

    @Override
    public String getAuthType() {
        return null;
    }

    public static Field nettyCookieField;

    static io.netty.handler.codec.http.cookie.Cookie getNettyCookie(io.vertx.ext.web.Cookie cookie) {
        if (nettyCookieField == null) {
            Field[] fields = io.vertx.ext.web.Cookie.class.getDeclaredFields();
            Field.setAccessible(fields, true);
            for (Field field : fields) {
                if ("nettyCookie".equals(field.getName())) {
                    nettyCookieField = field;
                }
            }
        }
        try {
            return (io.netty.handler.codec.http.cookie.Cookie) nettyCookieField.get(cookie);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Cookie cookie(io.vertx.ext.web.Cookie vertxCookie) {
        io.netty.handler.codec.http.cookie.Cookie nettyCookie = getNettyCookie(vertxCookie);
        if (nettyCookie == null) {
            return null;
        }
        Cookie cookie = new Cookie(vertxCookie.getName(), vertxCookie.getValue());

        cookie.setDomain(nettyCookie.domain());
        cookie.setHttpOnly(nettyCookie.isHttpOnly());
        cookie.setMaxAge((int) nettyCookie.maxAge());
        cookie.setPath(nettyCookie.path());
        cookie.setSecure(nettyCookie.isSecure());
        return cookie;
    }

    @Override
    public Cookie[] getCookies() {
        Set<io.vertx.ext.web.Cookie> vCookies = event.cookies();
        List<Cookie> cookies = vCookies.stream().map(VertxHttpServletRequest::cookie)
                .filter(c -> c != null).collect(toList());
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    @Override
    public long getDateHeader(String s) {
        String value = getHeader(s);
        if (value == null) {
            return -1;
        }
        final long date = DateParser.parseDate(value);
        if (date == -1)
            throw new IllegalArgumentException("Cannot convert date: " + value);
        return date;
    }

    @Override
    public String getHeader(String s) {
        return httpServerRequest.getHeader(s);
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        List<String> header = httpServerRequest.headers().getAll(s);
        return Collections.enumeration(header);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> headerNames = httpServerRequest.headers().names();
        return Collections.enumeration(headerNames);
    }

    @Override
    public int getIntHeader(String s) {
        String headerValue = httpServerRequest.getHeader(s);
        return headerValue == null ? -1 : Integer.parseInt(headerValue);
    }

    @Override
    public String getMethod() {
        return httpServerRequest.rawMethod();
    }

    @Override
    public String getPathInfo() {
        return httpServerRequest.path();
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getQueryString() {
        return httpServerRequest.query();
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return httpServerRequest.uri();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(httpServerRequest.absoluteURI());
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return httpServerRequest.getFormAttribute(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(httpServerRequest.formAttributes().names());
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        String contentLength = httpServerRequest.headers().get("content-lengh");
        if (contentLength != null) {
            return Integer.parseInt(contentLength);
        }
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        String contentLength = httpServerRequest.headers().get("content-lengh");
        if (contentLength != null) {
            return Long.parseLong(contentLength);
        }
        return 0;
    }

    @Override
    public String getContentType() {
        return httpServerRequest.headers().get("content-type");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(event.getBody().getBytes());
        return new VertxServletInputStream(is);
    }

    @Override
    public String getParameter(String s) {
        return httpServerRequest.getParam(s);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(httpServerRequest.params().names());
    }

    @Override
    public String[] getParameterValues(String s) {
        List<String> param = httpServerRequest.params().getAll(s);
        return param.toArray(new String[param.size()]);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        MultiMap map = httpServerRequest.params();
        Map<String, String[]> parameterMap = new HashMap<>();
        for (String name : httpServerRequest.params().names()) {
            List<String> values = map.getAll(name);
            parameterMap.put(name, values.toArray(new String[values.size()]));
        }
        return parameterMap;
    }

    @Override
    public String getProtocol() {
        return httpServerRequest.version().name().replaceFirst("_", "/").replaceAll("_", ".");
    }

    @Override
    public String getScheme() {
        return httpServerRequest.scheme();
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(event.getBody().getBytes());
        return new BufferedReader(new InputStreamReader(is));
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddress;
    }

    @Override
    public String getRemoteHost() {
        return httpServerRequest.remoteAddress().host();
    }

    @Override
    public void setAttribute(String s, Object o) {
        httpServerRequest.formAttributes().set(s, String.valueOf(o));
    }

    @Override
    public void removeAttribute(String s) {
        httpServerRequest.formAttributes().remove(s);
    }

    @Override
    public Locale getLocale() {
        io.vertx.ext.web.Locale locale = event.preferredLocale();
        return new Locale(locale.language(), locale.country(), locale.variant());
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(event.acceptableLocales().stream()
                .map(locale ->
                        new Locale(locale.language(), locale.country(), locale.variant()))
                .collect(toList()));
    }

    @Override
    public boolean isSecure() {
        return "https".equalsIgnoreCase(httpServerRequest.scheme());
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return httpServerRequest.remoteAddress() == null ? 0 : httpServerRequest.remoteAddress().port();
    }

    @Override
    public String getLocalName() {
        return httpServerRequest.localAddress() == null ? "" : httpServerRequest.localAddress().host();
    }

    @Override
    public String getLocalAddr() {
        return httpServerRequest.localAddress().toString();
    }

    @Override
    public int getLocalPort() {
        return httpServerRequest.localAddress().port();
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }
}
