import com.google.common.collect.Lists;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SandBoxLambdatek {

    private static DefaultHttpClient httpClient = createClient();
    private static String entryPoint = "https://www.lambda-tek.com/shop/?region=GB&catid=225";//https://www.bodeboca.com
    private static String host;
    private static String userAgent;

    private final static String PROXY = "proxy-out.workit.fr"; //"89.42.27.22";
    private final static int PROXY_PORT = 3129; //38953;

    private final static String USER = "workit";//workit
    private final static String PASSWD = "jUbunUqUG";//jUbunUqUG

    private static int tryConn = 0;

    private static int stats = 0;
    private static int stat200 = 0;
    private static int stat403 = 0;
    private static int stat502 = 0;
    private static int stat503 = 0;
    private static int stat504 = 0;
    private static int statOkContent = 0;
    private static int statNotOkContent = 0;

    private static DateTime dateTimeInit = DateTime.now();
    private static DateTime dateTimeEnd = DateTime.now().plusMinutes(5);
    private static Timer timer = new Timer();

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        configureHostDomain();
        createClient();

        String timeStamp = String.valueOf(dateTimeInit);
        timeStamp = StringUtils.substringBefore(timeStamp, ".");
        timeStamp = StringUtils.replace(timeStamp, ":", "-");
        final BufferedWriter fichier = new BufferedWriter(new FileWriter("D:\\Sandbox\\" + host + "_" + timeStamp + ".txt"));
        fichier.write("---------------Begin---------------\n");
        fichier.append(String.valueOf(dateTimeInit));

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (this.scheduledExecutionTime() > dateTimeEnd.getMillis()) {
                    System.out.println("\n-------------Time out-------------\n");
                    timer.cancel();
                    try {
                        fichier.append("\n----------------End----------------\n");
                        fichier.append(String.valueOf(DateTime.now()));
                        fichier.append("\n-------------Statistics------------\n");
                        fichier.append("Total : ");
                        fichier.append(String.valueOf(stats));
                        fichier.append("\n200 : ");
                        fichier.append(String.valueOf(stat200));
                        fichier.append("\n\tOK : ");
                        fichier.append(String.valueOf(statOkContent));
                        fichier.append("\n\tNOK : ");
                        fichier.append(String.valueOf(statNotOkContent));
                        fichier.append("\n403 : ");
                        fichier.append(String.valueOf(stat403));
                        fichier.append("\n502 : ");
                        fichier.append(String.valueOf(stat502));
                        fichier.append("\n503 : ");
                        fichier.append(String.valueOf(stat503));
                        fichier.append("\n504 : ");
                        fichier.append(String.valueOf(stat504));
                        fichier.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                Page page = getConnection(entryPoint, "type");
                String content = page.getContent();
                int status = page.getStatus();
                boolean isOkContent = isOk(content);
                if (isOkContent) statOkContent++;
                else statNotOkContent++;
                System.out.println("Content Ok : " + isOkContent);

                long num = this.scheduledExecutionTime() - dateTimeInit.getMillis();
                long den = dateTimeEnd.getMillis() - dateTimeInit.getMillis();
                float percent = ((float) num / (float) den) * 100;
                percent = Math.round(percent);
                System.out.println("% : " + percent);

                try {
                    fichier.append("\n-----------------------------------\n");
                    fichier.append("Date       : ");
                    fichier.append(String.valueOf(DateTime.now()));
                    fichier.append("\nStatus     : ");
                    fichier.append(String.valueOf(status));
                    fichier.append("\nContent Ok : ");
                    fichier.append(String.valueOf(isOkContent));
                    fichier.append("\nRequest headers : \n");
                    fichier.append(String.valueOf(page.getRequestHeaders()));
                    fichier.append("\nResponse headers : \n");
                    fichier.append(String.valueOf(page.getResponseHeaders()));
                    fichier.append("\n\n\n\nContent : \n");
                    fichier.append(String.valueOf(page.getContent()));
                    fichier.append("\n\n\n\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        System.out.println("------------Start Timer-----------\n");
//        timer.schedule(task, 0, 10);
        timer.scheduleAtFixedRate(task, 0, 2000);
    }

    private static boolean isOk(String content) {
        if (StringUtils.containsIgnoreCase(content, "It appears that a program on your network is attempting to spider our site") && StringUtils.containsIgnoreCase(content, "This is only allowed by authorized sites")) {
            return false;
        } else return true;
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
        }
    }

    private static DefaultHttpClient createClient() {
//        userAgent = getUserAgent();
        DefaultHttpClient httpclient = new DefaultHttpClient();

        httpclient.getCredentialsProvider().setCredentials(
                new AuthScope(PROXY, PROXY_PORT),
                new UsernamePasswordCredentials(USER, PASSWD));

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

    private static Page getConnection(final String url, final String type) {
        int status = 0;
        int retry = 0;
        boolean isOkContent = false;
        System.out.println("Connect " + tryConn + " on " + type + " url : " + url);

//        String pageUrl = url;
        Page page = new Page();
        while ((status != HttpStatus.SC_OK || !isOkContent) && retry < 5) {
            ++tryConn;
            retry++;
            try {
                System.out.println("Retry : " + retry);
                HttpContext localContext = context();
                HttpGet httpget = getMethod(allowHttpRedirection(false), url);
                HttpResponse response = httpClient.execute(httpget, localContext);
                HttpEntity entity = response.getEntity();

                status = response.getStatusLine().getStatusCode();
                page.setStatus(status);
                stats++;

                String content = getContent(response, entity);
                isOkContent = isOk(content);

                System.out.println("Connection status : " + status);
                page.setContent(content);

                if (status == HttpStatus.SC_OK) {
                    stat200++;
                } else if (status == 403) {
                    stat403++;
                } else if (status == 502) {
                    stat502++;
                } else if (status == 503) {
                    stat503++;
                } else if (status == 504) {
                    stat504++;
                }

                String requestHeaders = getHeadersAsString(httpget.getAllHeaders());
                page.setRequestHeaders(requestHeaders);
                String responseHeaders = getHeadersAsString(response.getAllHeaders());
                page.setResponseHeaders(responseHeaders);

                if (entity != null) {
                    EntityUtils.consume(entity);
                }

                if (!isOkContent) {
                    httpClient.getConnectionManager().shutdown();
                    Thread.sleep(2500);
                    httpClient = createClient();
                }
            } catch (Exception exc) {
                System.out.println("Connection parse error [" + url + "] - Exc : " + exc);
            }
        }
        return page;
    }

    private static String getHeadersAsString(Header[] headers) {
        String allHeader = "";
        for (Header header : headers) {
            allHeader += StringUtils.isBlank(allHeader) ? "" : ";\n";
            allHeader += "\t" + header.getName() + " : " + header.getValue();
        }
        return allHeader;
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
//        httpGet.addHeader("Cache-Control", "max-age=0, no-cache");
//        httpGet.addHeader("Host", host);
//        httpGet.addHeader("Referer", "");
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");

        if (params != null) {
            httpGet.setParams(params);
        }
        return httpGet;
    }

    private static String getUserAgent() {
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(USER_AGENTS.size());
        return USER_AGENTS.get(index);
    }

    private static List<String> USER_AGENTS = Lists.newArrayList(
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 OPR/45.0.2552.888",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 OPR/45.0.2552.888",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36 OPR/44.0.2510.24085",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36 OPR/44.0.2510.24085",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0",
            "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0.3",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0.3",
            "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0.3",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0.3",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:53.0) Gecko/20100101 Firefox/53.0",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:52.0) Gecko/20100101 Firefox/52.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0",
            "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0",
            "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.113 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.113 Safari/537.36"
    );

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
        private String requestHeaders;
        private String responseHeaders;

        private Page() {
        }

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

        private String getRequestHeaders() {
            return requestHeaders;
        }

        private void setRequestHeaders(String requestHeaders) {
            this.requestHeaders = requestHeaders;
        }

        private String getResponseHeaders() {
            return responseHeaders;
        }

        private void setResponseHeaders(String responseHeaders) {
            this.responseHeaders = responseHeaders;
        }
    }
}

