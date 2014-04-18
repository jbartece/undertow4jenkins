package undertow4jenkins.listener;

import java.io.FileInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class TrustManagerImpl implements X509TrustManager {

    private X509Certificate certificate;

    public TrustManagerImpl() throws Exception {
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        this.certificate = (X509Certificate) certFact.generateCertificate(
                new FileInputStream("src/ssl/server.crt"));
    }

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        throw new UnsupportedOperationException("Client trust not supported");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        for (X509Certificate x509Certificate : xcs) {
            System.out.println("certificate: " + x509Certificate.getIssuerX500Principal().getName());
            if (certificate.getSubjectX500Principal().equals(x509Certificate.getIssuerX500Principal()))
                return;
        }

        throw new CertificateException("Untrusted certificate?");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{certificate};
    }
}
