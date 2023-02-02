package uz.cosmos.appkiabot.service;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.cosmos.appkiabot.payload.*;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Service
public class RequestService {
    final String API_PATH_KIA = "https://api.kia-motors.uz/";

    private RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);
    }
//    KIA

    public ResKiaNew getModels() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return restTemplate().getForObject(this.API_PATH_KIA + "models", ResKiaNew.class);
    }

    public ResKiaModelInfo getModelInfo(String infoPath) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return restTemplate().getForObject(this.API_PATH_KIA + infoPath, ResKiaModelInfo.class);
    }
}
