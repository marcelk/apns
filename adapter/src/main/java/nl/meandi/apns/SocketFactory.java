package nl.meandi.apns;

import sun.security.x509.X500Name;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

class SocketFactory {

    private final static String COMMON_NAME_PREFIX_DEVELOPMENT = "Apple Development IOS Push Services";

    private final static String COMMON_NAME_PREFIX_PRODUCTION = "Apple Push Services";

    public static SSLSocket createSocket(String hostPrefix, int port, String certificateFileName, String certificateFilePassword) {
        KeyStore keystore = loadKeystore(certificateFileName, certificateFilePassword);
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("sunx509");
            keyManagerFactory.init(keystore, certificateFilePassword.toCharArray());

            DevelopmentOrProduction developmentOrProduction = null;

            for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
                X509ExtendedKeyManager manager = (X509ExtendedKeyManager) keyManager;
                String[] aliases = manager.getClientAliases("RSA", null);
                if (aliases.length != 1) {
                    throw new RuntimeException("Certificate file should contain only one certificate chain");
                }
                String alias = aliases[0];
                X509Certificate certificate = manager.getCertificateChain(alias)[0];
                X500Name x500name = new X500Name(certificate.getSubjectX500Principal().getName());
                String commonName = x500name.getCommonName();
                if (commonName.startsWith(COMMON_NAME_PREFIX_DEVELOPMENT)) {
                    developmentOrProduction = DevelopmentOrProduction.DEVELOPMENT;
                } else if (commonName.startsWith(COMMON_NAME_PREFIX_PRODUCTION)) {
                    developmentOrProduction = DevelopmentOrProduction.PRODUCTION;
                }
            }

            if (developmentOrProduction == null) {
                throw new RuntimeException("Didn't find a valid Apple IOS Push Services certificate in the certificate file");
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            String host = hostPrefix;
            if (developmentOrProduction == DevelopmentOrProduction.DEVELOPMENT) {
                host += ".sandbox";
            }
            host += ".push.apple.com";

            return (SSLSocket) sslSocketFactory.createSocket(host, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyStore loadKeystore(String certificateFileName, String certificateFilePassword) {
        KeyStore keyStore;
        try (InputStream keystoreStream = new FileInputStream(certificateFileName)) {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(keystoreStream, certificateFilePassword.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return keyStore;
    }
}
