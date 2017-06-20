import static org.apache.http.protocol.HTTP.UTF_8;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.util.StreamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PostConnection {

    static String host = "";
    static String domain = "";

    static DefaultHttpClient httpClient = createClient();

    public static void main(String[] args) {
        int status = 0;
        int tryConn = 0;
        String cookie = "";

        String pageUrl = "http://www.madridhifi.com/category/ajax/";
        System.out.println("Post connect on url : " + pageUrl);

        while (status != HttpStatus.SC_OK && tryConn < 5) {
            ++tryConn;
            try {
                HttpContext localContext = context();
                HttpPost httppost = postMethod(allowHttpRedirection(false), pageUrl, cookie);
                Map<String, String> nameValuePairs = getParameters();
                ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> entry : nameValuePairs.entrySet()) {
                    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httppost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
                // httppost.setEntity(new
                // StringEntity("TS01a4fbe9_id=3&TS01a4fbe9_cr=15896c8786455972ebd2f0aa2bc7%3Abcdc%3A2O55cYET%3A1855289944&TS01a4fbe9_76=0&TS01a4fbe9_md=1&TS01a4fbe9_rf=0&TS01a4fbe9_ct=0&TS01a4fbe9_pd=0",
                // HTTP.UTF_8));
                HttpResponse response = httpClient.execute(httppost, localContext);
                HttpEntity entity = response.getEntity();

                status = response.getStatusLine().getStatusCode();
                System.out.println("Connection status : " + status);

                if (status == HttpStatus.SC_OK) {
                    System.out.println("Debut d'ecriture ...");
                    final String content = getContent(response, entity);
                    System.out.println(content);
                    BufferedWriter fichier = new BufferedWriter(new FileWriter("D:\\W2P\\ContentWeb\\Post\\" + host + ".html"));
                    fichier.write(content);
                    fichier.close();
                    System.out.println("Fin d'ecriture");

                    JsonNode jsObject = getjsDocument(content);
                    String htmlBody = jsObject.findPath("html").asText();
                    Document htmlDocument = Jsoup.parse(htmlBody);
                    Elements elements = htmlDocument.select(".product_item");
                    System.out.println(elements.size());
                    for (int i = 0; i < elements.size(); i++) {
                        System.out.println("Product : " + i);
                        System.out.println(elements.get(i));
                    }
                } else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) {
                    String location = getLocation(response);
                    System.out.println("Location : " + location);
                    pageUrl = location;
                } else if (status == HttpStatus.SC_NOT_FOUND) {
                    System.out.println("Page not found");
                    if (entity != null) {
                        EntityUtils.consume(entity);
                    }
                    break;
                }

                if (entity != null) {
                    EntityUtils.consume(entity);
                }
            } catch (Exception exc) {
                System.out.println("Connection parse error : " + exc);
            }
        }
    }

    private static Map<String, String> getParameters() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("page", "1");
        map.put("queryString", "/televisores/led/page-all/");
        map.put("num_products_show", "70");
        return map;
    }

//     private static String getParametersString() {
//      String sku = "39317593";
//     String parameter =
//     "<?xml version=\"1.0\" encoding=\"utf-8\"?><tns:KALAvailabilityRequest xmlns:tns=\"http://www.baur.de/KAL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.baur.de/KAL http://www.baur.de/content/kal/KALAvailabilityRequestSchema.xsd\"><Articles><Article><CompleteCatalogItemNo>21810791</CompleteCatalogItemNo><SizeAlphaText>0</SizeAlphaText><Std_Promotion>91</Std_Promotion><CustomerCompanyID>0</CustomerCompanyID></Article><Article><CompleteCatalogItemNo>75346472</CompleteCatalogItemNo><SizeAlphaText>2</SizeAlphaText><Std_Promotion>72</Std_Promotion><CustomerCompanyID>0</CustomerCompanyID></Article><Article><CompleteCatalogItemNo>75346472</CompleteCatalogItemNo><SizeAlphaText>3</SizeAlphaText><Std_Promotion>72</Std_Promotion><CustomerCompanyID>0</CustomerCompanyID></Article><Article><CompleteCatalogItemNo>75346472</CompleteCatalogItemNo><SizeAlphaText>4</SizeAlphaText><Std_Promotion>72</Std_Promotion><CustomerCompanyID>0</CustomerCompanyID></Article><Article><CompleteCatalogItemNo>75346472</CompleteCatalogItemNo><SizeAlphaText>5</SizeAlphaText><Std_Promotion>72</Std_Promotion><CustomerCompanyID>0</CustomerCompanyID></Article><Article><CompleteCatalogItemNo>75346472</CompleteCatalogItemNo><SizeAlphaText>6</SizeAlphaText><Std_Promotion>72</Std_Promotion><CustomerCompanyID>0</CustomerCompanyID></Article><Article><CompleteCatalogItemNo>75346472</CompleteCatalogItemNo><SizeAlphaText>7</SizeAlphaText><Std_Promotion>72</Std_Promotion><CustomerCompanyID>0</CustomerCompanyID></Article></Articles></tns:KALAvailabilityRequest>";
//     // String parameter =
//     //
//     "<?xml version=\"1.0\" encoding=\"utf-8\"?><tns:KALAvailabilityRequest xmlns:tns=\"http://www.baur.de/KAL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.baur.de/KAL http://www.baur.de/content/kal/KALAvailabilityRequestSchema.xsd\"><Articles><Article><CompleteCatalogItemNo>"
//     // + sku + "</CompleteCatalogItemNo><SizeAlphaText>0</SizeAlphaText><Std_Promotion>" + sku.substring(sku.length() - 2) +
//     // "</Std_Promotion><CustomerCompanyID>0</CustomerCompanyID></Article></Articles></tns:KALAvailabilityRequest>";
//     return parameter;
//     }

    private static HttpPost postMethod(HttpParams params, String url, String cookie) throws UnsupportedEncodingException, MalformedURLException {
        final URL urlPage = new URL(url);
        host = urlPage.getHost();
        domain = urlPage.getProtocol() + "://" + host;

        HttpPost httpPost = new HttpPost(url);
        // httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        // httpPost.addHeader("Accept-Encoding", "gzip, deflate");
        // httpPost.addHeader("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4");
        // httpPost.addHeader("Connection", "keep-alive");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        // httpPost.addHeader("Cookie", "PRUM_EPISODES=http://www.coriolis.com/telephones-mobiles/smartphones-4g/crosscall-trekker-x2-noir.html;");
        // if (StringUtils.isNotBlank(cookie)) {
        // httpPost.addHeader("Cookie", cookie);
        // }
        // httpPost.addHeader("Referer", "http://www.coriolis.com/telephones-mobiles/smartphones-4g/crosscall-trekker-x2-noir.html");
        // httpPost.addHeader("Host", host);
        // httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");
        // httpPost.addHeader("X-Requested-With", "XMLHttpRequest");
        // httpPost.addHeader("Origin", "http://www.legallais.com");
        // httpPost.addHeader("node", "c3776a88-c058-4c39-8370-21a9bd04db9a");
        // httpPost.addHeader("Pragma", "no-cache");
        // httpPost.addHeader("Cache-Control", "no-cache");
        // httpPost.addHeader("Content-Length", "0");
        // httpPost.addHeader("X-MicrosoftAjax", "Delta=true");

        if (params != null) {
            httpPost.setParams(params);
        }
        return httpPost;
    }

    private static HttpParams allowHttpRedirection(boolean redirection) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, redirection);
        params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
        return params;
    }

    private static DefaultHttpClient createClient() {
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

    private static HttpContext context() {
        CookieStore cookieStore = httpClient.getCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        return localContext;
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

    private static String getLocation(HttpResponse response) {
        Header locationHeader = response.getFirstHeader("Location");
        if (locationHeader != null) {
            String location = locationHeader.getValue();
            return cleanPath(location);
        }
        return null;
    }

    private static String cleanPath(String location) {
        if (location.contains("http")) return location;
        else {
            if (location.startsWith("/")) location = domain + location;
            else location = domain + "/" + location;
        }
        return location;
    }

    static JsonNode getjsDocument(String content) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
