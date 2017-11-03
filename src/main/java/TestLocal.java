import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import liquibase.util.StreamUtil;
import org.apache.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.protocol.HttpContext;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestLocal {

    private static DefaultHttpClient httpClient = createClient();
    private static String entryPoint = "https://www.bodeboca.com";
    private static String host;
    private static String siteDomain;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
//        configureHostDomain();
//        createClient();
//        Page newPage = getConnection(entryPoint, "type");
//        System.out.println(newPage.getContent());
        String PATTERN_IP_ADRESSE_URL = "https*://[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
        String url = "https://145.9.252.123/";
        Matcher matcher = Pattern.compile(PATTERN_IP_ADRESSE_URL).matcher(url);
        int status = 301;
        int count = 0;
        boolean isRedirect = (status == 301 || status == 302) && StringUtils.isNotBlank(url);
        if (matcher.find()) {
            System.out.println("isRedirect : " + isRedirect);
            while (isRedirect && count < 1) {
                System.out.println("Status : " + status);
                System.out.println("Location : " + url);
                if (count == 0) status = 302;
                if (count == 1) status = 301;
                if (count == 2) status = 302;
                if (count == 3) status = 200;
                if (count == 0) url = "http://185.169.233.200/";
                if (count == 1) url = "http://185.169.233.300/";
                if (count == 2) url = "http://185.169.233.400/";
                if (count == 3) url = "http://185.169.233.500/";
                count++;
            }
            System.out.println("Status : " + status);
            System.out.println("Location : " + url);
        } else if (isRedirect) {
            System.out.println("Redirect normally");
        }
    }

    private static void configureHostDomain() {
        URL url = null;
        try {
            url = new URL(entryPoint);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        host = url.getHost();
        final String protocol = url.getProtocol();
        siteDomain = protocol + "://" + host;
    }

    private static DefaultHttpClient createClient() {
        final String proxy = "89.42.27.22"; //"5.157.63.21";
        final int proxyPort = 38953; //55235;
        DefaultHttpClient httpclient = new DefaultHttpClient();

        httpclient.getCredentialsProvider().setCredentials(
                new AuthScope(proxy, proxyPort),
                new UsernamePasswordCredentials("workit", "jUbunUqUG"));

        HttpHost proxyHost = new HttpHost(proxy, proxyPort);

        HttpRoutePlanner routePlanner = new HttpRoutePlanner() {
            public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
                return new HttpRoute(target, null, new HttpHost(proxy, proxyPort), "https".equalsIgnoreCase(target.getSchemeName()));
            }
        };

        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        httpclient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxyHost);
        httpclient.setRoutePlanner(routePlanner);
        return httpclient;
    }

    private static Page getConnection(final String url, final String type) {
        int status = 0;
        int tryConn = 0;
        System.out.println("Connect on " + type + " url : " + url);

        String pageUrl = url;
        Page page = new Page();
        while (status != HttpStatus.SC_OK && tryConn < 5) {
            ++tryConn;
            try {
                HttpContext localContext = context();
                HttpGet httpget = getMethod(allowHttpRedirection(false), pageUrl);
                HttpResponse response = httpClient.execute(httpget, localContext);
                HttpEntity entity = response.getEntity();

                status = response.getStatusLine().getStatusCode();

                final String content = getContent(response, entity);
                System.out.println("Connection status : " + status);

                if (status == HttpStatus.SC_OK) {
                    page.setContent(content);
                } else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) {
                    String location = getLocation(response);
                    System.out.println("Location : " + location);
                    pageUrl = location;
                } else if (status == 403 || status == 503) {
                    httpClient.getConnectionManager().shutdown();
                    Thread.sleep(1000);
                    httpClient = createClient();
                }

                if (entity != null) {
                    EntityUtils.consume(entity);
                }
            } catch (Exception exc) {
                System.out.println("Connection parse error [" + url + "] - Exc : " + exc);
            }
        }
        return page;
    }

    private static String getLocation(HttpResponse response) {
        Header locationHeader = response.getFirstHeader("Location");
        if (locationHeader != null) {
            String location = locationHeader.getValue();
            return cleanPath(location);
        }
        return null;
    }

    private static String cleanPath(String path) {
        if (path.startsWith("//")) return "http:" + path;
        if (!path.startsWith("http")) return siteDomain + path;
        return path;
    }

    private static String getContent(HttpResponse response, HttpEntity entity) throws IOException {
        Header contentEncodingHeader = response.getFirstHeader("Content-Encoding");
        if (contentEncodingHeader != null) {
            String encoding = contentEncodingHeader.getValue();
            if (encoding.contains("gzip")) {
                InputStreamReader reader = new InputStreamReader(new GzipDecompressingEntity(entity).getContent(), "utf-8");
                String content = StreamUtil.getReaderContents(reader);
                return StringUtils.trim(content);
            }
        }
        return StringUtils.trim(EntityUtils.toString(entity));
    }

    private static HttpGet getMethod(HttpParams params, String url) {
        HttpGet httpGet = new HttpGet(url);
//        httpGet.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        httpGet.addHeader("Accept-Encoding", "gzip, deflate");
//        httpGet.addHeader("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4");
//        httpGet.addHeader("Connection", "keep-alive");
//        httpGet.addHeader("Host", host);
//        httpGet.addHeader("Referer", "");
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");

        if (params != null) {
            httpGet.setParams(params);
        }
        return httpGet;
    }

    private static HttpContext context() {
        CookieStore cookieStore = httpClient.getCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        return localContext;
    }

    private static HttpParams allowHttpRedirection(boolean redirection) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, redirection);
        params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
        return params;
    }

    private static class Page {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

