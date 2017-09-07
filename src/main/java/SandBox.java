import liquibase.util.StreamUtil;
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
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;

public class SandBox {

    private static DefaultHttpClient httpClient;
    private static String entryPoint = "https://www.tonerpreis.de/search/index";//https://www.bodeboca.com
    private static String host;
//    private static String siteDomain;

    private final static String PROXY = "proxy-out.workit.fr"; //"89.42.27.22";
    private final static int PROXY_PORT = 3129; //38953;

    private final static String USER = "workit";//workit
    private final static String PASSWD = "jUbunUqUG";//jUbunUqUG

    private static int tryConn = 0;

    private static int stats = 0;
    private static int stat200 = 0;
    private static int stat403 = 0;

    private static DateTime dateTimeInit = DateTime.now();
    private static DateTime dateTimeEnd = DateTime.now().plusMinutes(1);
    private static Timer timer = new Timer();

    public static void main(String[] args) throws Exception {
        configureHostDomain();
        createClient();
//        System.setProperty("jsse.enableSNIExtension", "false");

        final BufferedWriter fichier = new BufferedWriter(new FileWriter("D:\\Sandbox\\" + host + dateTimeInit.getMillis() + ".txt"));
        fichier.write("---------------Begin---------------\n");
        fichier.append(String.valueOf(dateTimeInit));

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (this.scheduledExecutionTime() > dateTimeEnd.getMillis()) {
                    timer.cancel();
                    try {
                        fichier.append("\n----------------End----------------\n");
                        fichier.append(String.valueOf(DateTime.now()));
                        fichier.append("\n-------------Statistics------------\n");
                        fichier.append("Total : ");
                        fichier.append(String.valueOf(stats));
                        fichier.append("\n200 : ");
                        fichier.append(String.valueOf(stat200));
                        fichier.append("\n403 : ");
                        fichier.append(String.valueOf(stat403));
                        fichier.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                Page page = getConnection(entryPoint, "type");
//                String content = page.getContent();
                int status = page.getStatus();
//                boolean isOkContent = isOk(content);

//                long num = this.scheduledExecutionTime() - dateTimeInit.getMillis();
//                long den = dateTimeEnd.getMillis() - dateTimeInit.getMillis();
//                float percent = ((float) num / (float) den) * 100;

//                System.out.println("% : " + percent);
//                System.out.println("Content Ok : " + isOkContent);

                try {
                    fichier.append("\n-----------------------------------\n");
                    fichier.append("Date       : ");
                    fichier.append(String.valueOf(DateTime.now()));
                    fichier.append("\nStatus     : ");
                    fichier.append(String.valueOf(status));
                    fichier.append("\nContent Ok : ");
//                    fichier.append(String.valueOf(isOkContent));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 0, 100);
    }

    private static boolean isOk(String content) {
        String selector = ".nice-menu.nice-menu-down";
        Document document = Jsoup.parse(content);
        return document.select(selector) != null;
    }

    private static void configureHostDomain() {
        URL url = null;
        try {
            url = new URL(entryPoint);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url != null) {
            host = url.getHost();
//            final String protocol = url.getProtocol();
//            siteDomain = protocol + "://" + host;
        }
    }

    private static DefaultHttpClient createClient() throws Exception {
        DefaultHttpClient httpclient = buildCustomHttpClient();

//        httpclient.getCredentialsProvider().setCredentials(
//                new AuthScope(PROXY, PROXY_PORT),
//                new UsernamePasswordCredentials(USER, PASSWD));

        HttpHost proxyHost = new HttpHost(PROXY, PROXY_PORT);

        HttpRoutePlanner routePlanner = new HttpRoutePlanner() {
            public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
                return new HttpRoute(target, null, new HttpHost(PROXY, PROXY_PORT), "https".equalsIgnoreCase(target.getSchemeName()));
            }
        };

        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        httpclient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxyHost);
        httpclient.setRoutePlanner(routePlanner);
        return httpclient;
    }

    private static DefaultHttpClient buildCustomHttpClient() throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, getTrustingManager(), new java.security.SecureRandom());

        SSLSocketFactory socketFactory = new SSLSocketFactory(sc);
        Scheme sch = new Scheme("https", 443, socketFactory);
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        return httpclient;
    }

    private static TrustManager[] getTrustingManager() {
        TrustManager[] trustAllCerts = new TrustManager[1];
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }

        };

        trustAllCerts[0] = trustManager;

        return trustAllCerts;
    }

    private static Page getConnection(final String url, final String type) {
        int status;
        System.out.println("Connect " + tryConn + " on " + type + " url : " + url);

//        String pageUrl = url;
        Page page = new Page();
//        while (status != HttpStatus.SC_OK && tryConn < 5) {
        ++tryConn;
        try {
            HttpContext localContext = context();
            HttpGet httpget = getMethod(allowHttpRedirection(false), url);
            HttpResponse response = httpClient.execute(httpget, localContext);
            HttpEntity entity = response.getEntity();

            status = response.getStatusLine().getStatusCode();
            page.setStatus(status);
            stats++;

            final String content = getContent(response, entity);
            System.out.println("Connection status : " + status);
            page.setContent(content);

            if (status == HttpStatus.SC_OK) {
                stat200++;
            } else if (status == 403 || status == 503) {
                stat403++;
            }

            if (entity != null) {
                EntityUtils.consume(entity);
            }
        } catch (Exception exc) {
            System.out.println("Connection parse error [" + url + "] - Exc : " + exc);
        }
//        }
        return page;
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
        private int status;
        private String content;

        private String getContent() {
            return content;
        }

        private void setContent(String content) {
            this.content = content;
        }

        private int getStatus() {
            return status;
        }

        private void setStatus(int status) {
            this.status = status;
        }
    }
}

