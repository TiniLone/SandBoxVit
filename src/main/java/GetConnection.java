import static org.apache.http.protocol.HTTP.UTF_8;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.util.StreamUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetConnection {

    static String host = "";
    static String session = "";
    static String domain = "";

    static String content = "";

    public static void main(String[] args) throws Exception {
        int status = 0;
        int tryConn = 0;
        boolean hasMorePage = true;

        String pageUrl = "https://www.tonerpreis.de/druckerpatronen/3949914am-tinte-kodak-3949914-no-10b-schwarz.html";
        pageUrl = pageUrl.replace(" ", "%20").replace("|", "%7C");
        System.out.println(pageUrl);

        DefaultHttpClient httpClient = createClient();

        while (hasMorePage) {
            while (status != HttpStatus.SC_OK && tryConn < 5) {
                ++tryConn;
                try {
                    System.out.println("Get connect on url : " + pageUrl);

                    CookieStore cookieStore = httpClient.getCookieStore();
                    HttpContext localContext = new BasicHttpContext();
                    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

                    HttpGet httpget = getMethod(allowHttpRedirection(false), pageUrl);
                    HttpResponse response = httpClient.execute(httpget, localContext);
                    HttpEntity entity = response.getEntity();

                    status = response.getStatusLine().getStatusCode();
                    System.out.println("Connection status : " + status);

                    // session = response.getFirstHeader("X-Crawlera-Session").getValue();

                    if (status == HttpStatus.SC_OK) {
                        System.out.println("Debut d'ecriture ...");
                        content = getContent(response, entity);
                        // System.out.println(content);
                        // JSONObject jsDocPrincipal = getjsDocument(Jsoup.parse(content));
                        // JSONArray jsDocArray = jsDocPrincipal.getJSONArray("data");
                        // System.out.println("" + jsDocArray);
                        BufferedWriter fichier = new BufferedWriter(new FileWriter("D:\\W2P\\ContentWeb\\" + host + ".html"));
                        fichier.write(content);
                        fichier.close();
                        // Elements contentScript = Jsoup.parse(content).select("script");
                        // String testSelector = getScript(contentScript, "ReevooLib.Data.callbacks");
                        // testSelector = StringUtils.substringAfter(testSelector, "reviewCount : ");
                        // testSelector = StringUtils.substringBefore(testSelector, ",").trim();
                        // System.out.println(">>>>>>> : " + testSelector);

                        // Elements offersElements = Jsoup.parse(content).select("[id=matchingItems] > div.productTeaser");
                        // System.out.println("Size : " + offersElements.size());
                        // Element nextElement = Jsoup.parse(content).select("a.next.last").last();
                        // System.out.println("Next : " + nextElement);
                        // if (nextElement != null) {
                        // hasMorePage = true;
                        // pageUrl = cleanPath(nextElement.attr("href"), domain);
                        // } else {
                        // hasMorePage = false;
                        // }
                        // Element product = Jsoup.parse(content).select("p.availability").first();
                        // System.out.println(">>> " + product);
                        // product = Jsoup.parse(content).select("div.add-to-cart > button").first();
                        // System.out.println(">>> " + product);
                        // String str = product.text();
                        // System.out.println(">>> " + str);
                        // product = Jsoup.parse(content).select("div#customer-reviews").first();
                        // System.out.println(">>> " + product);
                        // Element products = Jsoup.parse(content).select("div.box_advancereviews > dd table.ratings-table tr:contains(Qualidade) div.rating").first();
                        // System.out.println(">>> " + products.attr("style"));
                        // Element productDelivery = Jsoup.parse(products.attr("data-content"));
                        // System.out.println(">>> " + productDelivery);
                        // String deliveryRaw = productDelivery.select("span.box-left:contains(Kotiinkuljetus) + span.box-right").first().text();
                        // System.out.println(">>> " + deliveryRaw);
                        // System.out.println();
                        // float pageLimit = Float.parseFloat(testSelector) / 15;
                        // System.out.println(">>> " + pageLimit);
                        // System.out.println(">>> " + ((int) pageLimit + 1));
                        // if (testSelector.contains("total")) testSelector = testSelector.substring(testSelector.indexOf("of"));
                        // testSelector = testSelector.replaceAll("[^\\d+]", "").trim();
                        // System.out.println(">>> " + testSelector);
                        System.out.println("Fin d'ecriture");
                    } else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) {
                        String location = getLocation(response, domain);
                        System.out.println("Location : " + location);
                        pageUrl = location;
                    } else if (status == HttpStatus.SC_NOT_FOUND) {
                        System.out.println("Page not found");
                        if (entity != null) {
                            EntityUtils.consume(entity);
                        }
                        session = "";
                        return;
                    }

                    if (entity != null) {
                        EntityUtils.consume(entity);
                    }
                } catch (Exception exc) {
                    System.out.println("Connection parse error : " + exc);
                    status = 0;
                }
            }
            if (status == HttpStatus.SC_OK) {
                Elements offersElements = Jsoup.parse(content).select("[id=matchingItems] > div.productTeaser");
                System.out.println("Size : " + offersElements.size());
                for (Element offerElement : offersElements) {
                    System.out.println(offerElement.html());
                }
                Element nextElement = Jsoup.parse(content).select("a.next.last").first();
                System.out.println("Next : " + nextElement);
                if (nextElement != null) {
                    hasMorePage = true;
                    status = 0;
                    pageUrl = cleanPath(nextElement.attr("href"), domain);
                } else {
                    hasMorePage = false;
                }
            }
        }
    }

    private static HttpParams allowHttpRedirection(boolean redirection) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, redirection);
        params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
        return params;
    }

    private static HttpGet getMethod(HttpParams params, String url) throws MalformedURLException {
        final URL urlPage = new URL(url);
        host = urlPage.getHost();
        domain = urlPage.getProtocol() + "://" + host;

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.addHeader("Accept-Encoding", "gzip, deflate");
        httpGet.addHeader("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4");
        httpGet.addHeader("Connection", "keep-alive");
        // httpGet.addHeader("X-Requested-With", "XMLHttpRequest");
        // httpGet.addHeader("Content-Type", "application/json");
        // httpGet.addHeader("Cache-Control", "no-cache");
        // httpGet.addHeader("X-Crawlera-UA", "pass");
        // httpGet.addHeader("X-Crawlera-Cookies", "disable");
        // httpGet.addHeader("Cookie",
        // "__as_rng=42; setavid=1; cookieBannerAccepted=true; av-logging-sample-rate=52; pageType=category; ABTasty=uid%3D16121412560589538%26fst%3D1481709365262%26pst%3D1481802630962%26cst%3D1481861454439%26ns%3D9%26pvt%3D48%26pvis%3D4%26th%3D159168.220145.5.0.9.0.1481709365271.1481714720355.1_159294.220309.5.0.8.0.1481714256722.1481802853002.1_159950.221166.41.4.6.1.1481775039525.1481861746793.1; av-third-party-enabled=true; ABTastySession=referrer%3D__landingPage%3Dhttp%3A//www.delamaison.fr/verre-verre-souffle-bouche-pieces-ceralacca-p-101918.html%23p%3D101915__referrerSent%3Dtrue; lil_ids=MTNmY2EwMjYtM2U2Ny00NWY1LWJjODUtNjA0MTFiM2MyOTk2OmZmNTM2ZjQyLTgwZTYtNDViZC1hOTc4LWIyNWYzMmEwOGI2Yw%3D%3D; TW_SESSION_ID=9b7174fc-4d40-44bb-981e-03a135bfa808; TW_SESSION_SEQUENCE=2; TW_VISITOR_ID=563e5960-3adc-41bd-a1c6-e3dd92c6f016; _cae=eyJ0IjpbXX0.; _ga=GA1.2.188747478.1481709365; rwd_session_prod=f150e657e984a05b6ccbaddd114796310a71f189");
        // httpGet.addHeader("Pragma", "no-cache");
        httpGet.addHeader("Host", host);
        // httpGet.addHeader("Origin", "http://www.efacil.com.br");
        // httpGet.addHeader("Referer",
        // "http://www.efacil.com.br/loja/produto/eletroportateis/Fritadeira/fritadeira-wellness-fry-3-2-litros-preta-mallory-p2214179/?loja=uberlandia");
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

        if (params != null) {
            httpGet.setParams(params);
        }
        return httpGet;
    }

    private static String getContent(HttpResponse response, HttpEntity entity) throws IOException {
        Header contentEncodingHeader = response.getFirstHeader("Content-Encoding");
        if (contentEncodingHeader != null) {
            String encoding = contentEncodingHeader.getValue();
            if (encoding.contains("gzip")) {
                InputStreamReader reader = new InputStreamReader(new GzipDecompressingEntity(entity).getContent(), UTF_8);
                String content = StreamUtil.getReaderContents(reader);
                return StringUtils.trim(content);
            }
        }
        return StringUtils.trim(EntityUtils.toString(entity, UTF_8));
    }

    private static String getLocation(HttpResponse response, String domain) {
        Header locationHeader = response.getFirstHeader("Location");
        if (locationHeader != null) {
            String location = locationHeader.getValue();
            return cleanPath(location, domain);
        }
        return null;
    }

    private static String cleanPath(String location, String domain) {
        if (location.contains("http")) return location;
        else {
            if (location.startsWith("/")) location = domain + location;
            else location = domain + "/" + location;
        }
        return location;
    }

    // private static String getScript(Elements scriptsElements, String content) {
    // for (Element scriptElement : scriptsElements) {
    // String script = scriptElement.data();
    // if (!StringUtils.isEmpty(script)) {
    // if (script.contains(content)) {
    // return script.trim();
    // }
    // }
    // }
    // return null;
    // }

    private static DefaultHttpClient createClient() throws Exception {
        // DefaultHttpClient httpclient = buildCustomHttpClient();
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpHost proxyHost = new HttpHost("proxy-out.workit.fr", 3129);

        HttpRoutePlanner routePlanner = new HttpRoutePlanner() {
            @Override
            public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
                return new HttpRoute(target, null, new HttpHost("proxy-out.workit.fr", 3129), "https".equalsIgnoreCase(target.getSchemeName()));
            }
        };

        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        httpclient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxyHost);
        httpclient.setRoutePlanner(routePlanner);
        return httpclient;
    }

    // private static DefaultHttpClient buildCustomHttpClient() throws Exception {
    // DefaultHttpClient httpclient = new DefaultHttpClient();
    //
    // SSLContext sc = SSLContext.getInstance("TLSv1.2");
    // sc.init(null, getTrustingManager(), new java.security.SecureRandom());
    //
    // SSLSocketFactory socketFactory = new SSLSocketFactory(sc);
    // Scheme sch = new Scheme("https", 443, socketFactory);
    // httpclient.getConnectionManager().getSchemeRegistry().register(sch);
    // return httpclient;
    // }

    // private static TrustManager[] getTrustingManager() {
    // TrustManager[] trustAllCerts = new TrustManager[1];
    // X509TrustManager trustManager = new X509TrustManager() {
    // @Override
    // public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    // return null;
    // }
    //
    // @Override
    // public void checkClientTrusted(X509Certificate[] certs, String authType) {
    // // Do nothing
    // }
    //
    // @Override
    // public void checkServerTrusted(X509Certificate[] certs, String authType) {
    // // Do nothing
    // }
    //
    // };
    //
    // trustAllCerts[0] = trustManager;
    //
    // return trustAllCerts;
    // }

    static JsonNode getjsDocument(Element document) {
        String scriptData = getScriptData(document);
        String scriptContentFromData = extractContentFromData(scriptData);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(scriptContentFromData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String extractContentFromData(String scriptData) {
        String content = StringUtils.substringAfterLast(scriptData, "service.processResult({\"data\":[{\"name\":");
        content = "{\"data\":[{\"name\":" + content;
        content = StringUtils.substringBefore(content, "return");
        content = StringUtils.substringBeforeLast(content, ")");
        return content;
    }

    private static String getScriptData(Element document) {
        Elements eltScripts = document.select("script");
        for (Element eltScript : eltScripts) {
            if (StringUtils.containsIgnoreCase(eltScript.data(), "service.processResult({\"data\":[{\"name\"")) return eltScript.data();
        }
        return null;
    }

}
