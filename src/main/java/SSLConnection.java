import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * java SSL-SNI resolution issue
 *
 */
public class SSLConnection {

    private static void makeSSLCall(final String URL) throws Exception {

        System.setProperty("jsse.enableSNIExtension", "false");
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

            // set up a TrustManager that trusts everything
            sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    System.out.println("getAcceptedIssuers =============");
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    System.out.println("checkClientTrusted =============");
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    System.out.println("checkServerTrusted =============");
                }
            } }, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

                public boolean verify(String arg0, SSLSession arg1) {
                    System.out.println("hostnameVerifier =============");
                    return true;
                }
            });
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-out.workit.fr", 3129));

            URL url = new URL(URL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxy);
            System.out.println("STATUS = " + conn.getResponseCode());

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            makeSSLCall("https://hausgeraete-panitz.de/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
