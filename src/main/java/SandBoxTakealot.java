import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class SandBoxTakealot {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {

        try {
            int status = 200;
            int count = 0;
            while (status == 200) {
                DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
                HttpHost proxy = new HttpHost("5.135.195.166", 3128);
                defaultHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                HttpGet httpGet = new HttpGet("https://api.takealot.com/rest/v-1-6-0/productlines/search?sort=BestSelling%20Descending&rows=10&start=0&detail=mlisting&backend=arj-fbye-zz-fla-fcenax&filter=Type:12&filter=Category:10942&filter=Available:true");
                count++;
                CloseableHttpResponse response = defaultHttpClient.execute(httpGet);
                status = response.getStatusLine().getStatusCode();
                defaultHttpClient.getConnectionManager().closeExpiredConnections();
                System.out.println("Count : " + count + " - status : " + status);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}