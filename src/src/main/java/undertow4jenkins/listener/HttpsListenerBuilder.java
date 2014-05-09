package undertow4jenkins.listener;

import io.undertow.Undertow.Builder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;
import undertow4jenkins.option.Options;

@SuppressWarnings("restriction")
public class HttpsListenerBuilder {

    /**
     * IP address, which should be set to bind listener in Undertow to listen on all interfaces
     */
    private static final String hostAllInterfacesString = "0.0.0.0";

    private static final int MAX_PORT = 65535;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Options options;

    public HttpsListenerBuilder(Options options) {
        this.options = options;
    }

    // "   --httpsKeepAliveTimeout   = how long idle HTTPS keep-alive connections are kept around (in ms; default 5000)?\n" +

    // "   --httpsPort              = set the https listening port. -1 to disable, Default is disabled\n" +
    // "                              if neither --httpsCertificate nor --httpsKeyStore are specified,\n" +
    // "                              https is run with one-time self-signed certificate.\n" +
    // "   --httpsListenAddress     = set the https listening address. Default is all interfaces\n" +
    // "   --httpsKeyManagerType    = the SSL KeyManagerFactory type (eg SunX509, IbmX509). Default is SunX509\n" +
    public void setHttpsListener(Builder serverBuilder) {
        if (options.httpsPort == -1)
            return;

        if (options.httpsPort < -1 || options.httpsPort > MAX_PORT) {
            log.warn("Unallowed httpsPort value. HTTPS listener is disabled!");
            return;
        }

        String host;
        if (options.httpsListenAdress == null)
            host = hostAllInterfacesString; // Listen on all interfaces
        else
            host = options.httpsListenAdress;

        if (options.httpsKeyStore != null) {
            createHttpsListenerWithKeyStore(serverBuilder, host, options.httpsPort);
        }
        else {
            if (options.httpsCertificate != null)
                createHttpsListenerWithCert(serverBuilder, host, options.httpsPort);
            else
                createHttpsListenerWithSelfSignedCert(serverBuilder, host, options.httpsPort);

        }
        
        log.info("HTTPS listener created.");

    }

    private void createHttpsListenerWithSelfSignedCert(Builder serverBuilder, String host,
            Integer httpsPort) {
        log.info("Using one-time self-signed certificate");
        try {
            char[] keyStorePassword = "changeit".toCharArray();

            CertAndKeyGen certKeyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            certKeyGen.generate(1024);
            PrivateKey privateKey = certKeyGen.getPrivateKey();

            X500Name xName = new X500Name("Test site", "Unknown", "Unknown", "Unknown");
            X509Certificate certificate = certKeyGen
                    .getSelfCertificate(xName, 3650L * 24 * 60 * 60);

            KeyStore keyStoreInstance = KeyStore.getInstance("JKS");
            keyStoreInstance.load(null);
            keyStoreInstance.setKeyEntry("hudson", privateKey, keyStorePassword,
                    new Certificate[] { certificate });
            
            SSLContext sslContext = createSSLContext(keyStorePassword, keyStoreInstance);
            serverBuilder.addHttpsListener(options.httpsPort, host, sslContext);
        } catch (Exception e) {
            log.warn("Failed to init SSL context. Check HTTPS options. HTTPS listener disabled!", e);
        }
    }

    // B:
    // "   --httpsCertificate       = the location of the PEM-encoded SSL certificate file.\n" +
    // "                              (the one that starts with '-----BEGIN CERTIFICATE-----')\n" +
    // "                              must be used with --httpsPrivateKey.\n" +
    // "   --httpsPrivateKey        = the location of the PEM-encoded SSL private key.\n" +
    // "                              (the one that starts with '-----BEGIN RSA PRIVATE KEY-----')\n" +
    private void createHttpsListenerWithCert(Builder serverBuilder, String host,
            Integer httpsPort) {
        if (options.httpsPrivateKey == null) {
            log.warn("HttpsPrivate has to be set to enable HTTPS listener with certificate. "
                    + "HTTPS listener is disabled!");
            return;
        }

        File certFile = new File(options.httpsCertificate);
        File privateKeyFile = new File(options.httpsPrivateKey);

        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X509");
            Certificate cert = certFactory.generateCertificate(new FileInputStream(certFile));
            PrivateKey privateKey = createRSAPrivateKeyFromCert(new FileReader(privateKeyFile));

            char[] keyStorePassword = "changeit".toCharArray();

            KeyStore keyStoreInstance = KeyStore.getInstance("JKS");
            keyStoreInstance.load(null);
            keyStoreInstance.setKeyEntry("hudson", privateKey, keyStorePassword,
                    new Certificate[] { cert });

            SSLContext sslContext = createSSLContext(keyStorePassword, keyStoreInstance);
            serverBuilder.addHttpsListener(options.httpsPort, host, sslContext);

        } catch (Exception e) {
            log.warn("Failed to init SSL context. Check HTTPS options. HTTPS listener disabled!", e);
        }

    }

    private PrivateKey createRSAPrivateKeyFromCert(FileReader fileReader) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BASE64Decoder decoder = new BASE64Decoder();

        try {
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            boolean in = false;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("-----")) {
                    in = !in;
                    continue;
                }
                if (in) {
                    outStream.write(decoder.decodeBuffer(line));
                }
                // Another option to decode Base64 data: javax.xml.bind.DatatypeConverter
                // http://stackoverflow.com/questions/469695/decode-base64-data-in-java
            }
        } finally {
            fileReader.close();
        }

        DerInputStream dis = new DerInputStream(outStream.toByteArray());
        DerValue[] seq = dis.getSequence(0);

        BigInteger mod = seq[1].getBigInteger();
        BigInteger privExpo = seq[3].getBigInteger();

        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(new RSAPrivateKeySpec(mod, privExpo));
    }

    // A:
    // "   --httpsKeyStore          = the location of the SSL KeyStore file.\n" +
    // "   --httpsKeyStorePassword  = the password for the SSL KeyStore file. Default is null\n" +
    private void createHttpsListenerWithKeyStore(Builder serverBuilder, String host,
            Integer httpsPort) {

        char[] keyStorePassword;
        if (options.httpsKeyStorePassword == null)
            keyStorePassword = null;
        else
            keyStorePassword = options.httpsKeyStorePassword.toCharArray();

        File keyStoreFile = new File(options.httpsKeyStore);
        if (!keyStoreFile.exists() || !keyStoreFile.isFile()) {
            log.warn("KeyStore not found. HTTPS listener disabled!");
            return;
        }

        try {
            KeyStore keyStoreInstance = KeyStore.getInstance("JKS");
            keyStoreInstance.load(new FileInputStream(keyStoreFile), keyStorePassword);

            SSLContext sslContext = createSSLContext(keyStorePassword, keyStoreInstance);
            serverBuilder.addHttpsListener(options.httpsPort, host, sslContext);

        } catch (Exception e) {
            log.warn("Failed to init SSL context. Check HTTPS options. HTTPS listener disabled!", e);
        }
    }

    private SSLContext createSSLContext(char[] keyStorePassword, KeyStore keyStoreInstance)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException,
            KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory
                .getInstance(options.httpsKeyManagerType);
        keyManagerFactory.init(keyStoreInstance, keyStorePassword);

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        return sslContext;
    }
}
