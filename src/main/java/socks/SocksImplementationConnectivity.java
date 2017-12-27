package socks;

import liquibase.util.StreamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class SocksImplementationConnectivity {

    public static void main(String[] args) {
        System.out.println(connectUsingSocketFactoryDefault("http://www.google.com"));
    }

    private static String connectUsingSocketFactoryDefault(String url) {
        String content = null;
        CloseableHttpClient closeableHttpClient = createClientWithSocks();
        InetSocketAddress socketAddress = new InetSocketAddress("mysockshote", 1234);
        HttpClientContext clientContext = HttpClientContext.create();
        clientContext.setAttribute("socks.address", socketAddress);

        HttpHost target = new HttpHost("localhost", 80, "http");
        HttpGet request = new HttpGet(url);

        System.out.println("Executing request " + request + " to " + target + " via SOCKS proxy " + socketAddress);
        try {
            CloseableHttpResponse response = closeableHttpClient.execute(request, clientContext);
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            content = getContent(response, entity);
            EntityUtils.consume(entity);
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
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

    private static CloseableHttpClient createClientWithSocks() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new MyConnectionSocketFactoryDefault(SSLContexts.createSystemDefault()))
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }

}
