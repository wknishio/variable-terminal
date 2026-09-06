package org.vash.vate.tls;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.vash.vate.org.bouncycastle.asn1.x500.X500Name;
import org.vash.vate.org.bouncycastle.asn1.x500.X500NameBuilder;
import org.vash.vate.org.bouncycastle.asn1.x500.style.BCStyle;
import org.vash.vate.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.vash.vate.org.bouncycastle.asn1.x509.BasicConstraints;
import org.vash.vate.org.bouncycastle.asn1.x509.Extension;
import org.vash.vate.org.bouncycastle.asn1.x509.GeneralName;
import org.vash.vate.org.bouncycastle.asn1.x509.GeneralNames;
import org.vash.vate.org.bouncycastle.cert.X509v3CertificateBuilder;
import org.vash.vate.org.bouncycastle.cert.bc.BcX509v3CertificateBuilder;
import org.vash.vate.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.vash.vate.org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.vash.vate.org.bouncycastle.crypto.util.PublicKeyFactory;
import org.vash.vate.org.bouncycastle.operator.ContentSigner;
import org.vash.vate.org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.vash.vate.org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.vash.vate.org.bouncycastle.operator.bc.BcDSAContentSignerBuilder;
import org.vash.vate.org.bouncycastle.operator.bc.BcECContentSignerBuilder;
import org.vash.vate.org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.vash.vate.org.bouncycastle.util.encoders.Base32;
import org.vash.vate.org.bouncycastle.util.encoders.Base64;
import org.vash.vate.reflection.VTReflectionUtils;

public class VTTLSUtilities
{
  private static class OverlyOptimisticHostnameVerifier implements HostnameVerifier
  {
    public boolean verify(String arg0, SSLSession arg1)
    {
      return true;
    }
  }
  
  @SuppressWarnings("all")
  private static class OverlyOptimisticTrustManager implements TrustManager, X509TrustManager
  {
    public java.security.cert.X509Certificate[] getAcceptedIssuers()
    {
      return new java.security.cert.X509Certificate[] {};
    }
    
    public boolean isServerTrusted(java.security.cert.X509Certificate[] certs)
    {
      return true;
    }
    
    public boolean isClientTrusted(java.security.cert.X509Certificate[] certs)
    {
      return true;
    }
    
    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
    {
      return;
    }
    
    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
    {
      return;
    }
  }
  
  @SuppressWarnings("all")
  private static class OverlyOptimisticKeyManager implements KeyManager
  {
    public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2)
    {
      return "";
    }
    
    public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2)
    {
      return "";
    }
    
    public X509Certificate[] getCertificateChain(String arg0)
    {
      return new X509Certificate[] {};
    }
    
    public String[] getClientAliases(String arg0, Principal[] arg1)
    {
      return new String[] {};
    }
    
    public PrivateKey getPrivateKey(String arg0)
    {
      return null;
    }
    
    public String[] getServerAliases(String arg0, Principal[] arg1)
    {
      return new String[] {};
    }
  }
  
  public static void allowUnsafeTLSSettings()
  {
    try
    {
      java.security.Security.setProperty("jdk.certpath.disabledAlgorithms", "");
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "");
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      java.security.Security.setProperty("jdk.crypto.disabledAlgorithms", "");
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      java.security.Security.setProperty("jdk.security.legacyAlgorithms", "");
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      java.security.Security.setProperty("jdk.tls.legacyAlgorithms", "");
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public static boolean disableHttpsTLSVerifications()
  {
    try
    {
      System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
      System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
      //System.setProperty("jsse.enableSNIExtension", "false");
      //System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
      //System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
      
      TrustManager[] trustAnything = new TrustManager[] {new OverlyOptimisticTrustManager()};
      SSLContext unsafeTLSContext = SSLContext.getInstance("TLS");
      unsafeTLSContext.init(null, trustAnything, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(unsafeTLSContext.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier(new OverlyOptimisticHostnameVerifier());
      try
      {
        Method setDefaultMethod = SSLContext.class.getDeclaredMethod("setDefault", SSLContext.class);
        setDefaultMethod.invoke(null, unsafeTLSContext);
      }
      catch (Throwable ei)
      {
        
      }
    }
    catch (Throwable e)
    {
      return false;
    }
    return true;
  }
  
  public static boolean supportsAtLeastJDK6()
  {
    return VTReflectionUtils.getJavaVersion() >= 6;
  }
  
  public static boolean supportsAtLeastJDK7()
  {
    return VTReflectionUtils.getJavaVersion() >= 7;
  }
  
  public static boolean supportsAtLeastJDK8()
  {
    return VTReflectionUtils.getJavaVersion() >= 8;
  }
  
  public static KeyPair createKeyPair(String algorithm, int keySizeBits, String parameterSpec)
  {
    try
    {
      KeyPairGenerator keyGen = null;
      if (keySizeBits <= 1024)
      {
        keySizeBits = 1024;
      }
      if (!supportsAtLeastJDK6())
      {
        //JDK 5 capabilities
        if (algorithm.equalsIgnoreCase("DSA"))
        {
          keyGen = KeyPairGenerator.getInstance(algorithm);
          keyGen.initialize(Math.min(keySizeBits, 1024));
        }
        else
        {
          keyGen = KeyPairGenerator.getInstance("RSA");
          keyGen.initialize(Math.min(keySizeBits, 2048));
        }
      }
      else if (!supportsAtLeastJDK7())
      {
        //JDK 6 capabilities
        if (algorithm.equalsIgnoreCase("DSA"))
        {
          keyGen = KeyPairGenerator.getInstance(algorithm);
          keyGen.initialize(Math.min(keySizeBits, 1024));
        }
        else
        {
          keyGen = KeyPairGenerator.getInstance("RSA");
          keyGen.initialize(Math.min(keySizeBits, 4096));
        }
      }
      else if (!supportsAtLeastJDK8())
      {
        //JDK 7 capabilities
        if (algorithm.equalsIgnoreCase("EC"))
        {
          keyGen = KeyPairGenerator.getInstance(algorithm);
          ECGenParameterSpec ecSpec = new ECGenParameterSpec(parameterSpec);
          keyGen.initialize(ecSpec);
        }
        else if (algorithm.equalsIgnoreCase("DSA"))
        {
          keyGen = KeyPairGenerator.getInstance(algorithm);
          keyGen.initialize(Math.min(keySizeBits, 1024));
        }
        else
        {
          keyGen = KeyPairGenerator.getInstance("RSA");
          keyGen.initialize(Math.min(keySizeBits, 4096));
        }
      }
      else
      {
        //JDK 8+ capabilities
        if (algorithm.equalsIgnoreCase("EC"))
        {
          keyGen = KeyPairGenerator.getInstance(algorithm);
          ECGenParameterSpec ecSpec = new ECGenParameterSpec(parameterSpec);
          keyGen.initialize(ecSpec);
        }
        else if (algorithm.equalsIgnoreCase("DSA"))
        {
          keyGen = KeyPairGenerator.getInstance(algorithm);
          keyGen.initialize(Math.min(keySizeBits, 2048));
        }
        else
        {
          keyGen = KeyPairGenerator.getInstance("RSA");
          keyGen.initialize(Math.min(keySizeBits, 4096));
        }
      }
      KeyPair keyPair = keyGen.generateKeyPair();
      return keyPair;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
  
  public static byte[] createSelfSignedTLSCertificateEncodedData(KeyPair keyPair)
  {
    try
    {
      String keyAlgorithm = keyPair.getPublic().getAlgorithm();
      
      String uuid = UUID.randomUUID().toString();
      String commonName = Base64.toBase64String(uuid.getBytes());
      String dnsName = Base32.toBase32String(uuid.replaceFirst("-", "").getBytes());
      
      AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(keyPair.getPublic().getEncoded());
      AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
      
      X500Name dnName = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, commonName).build();
      
      long now = System.currentTimeMillis();
      Date validityStartDate = new Date(now - (30L * 24 * 60 * 60 * 1000));
      Date validityEndDate = new Date(now + (90L * 24 * 60 * 60 * 1000));
      
      BigInteger serialNumber = new BigInteger(128, new SecureRandom());
      
      X509v3CertificateBuilder certBuilder = new BcX509v3CertificateBuilder(dnName, serialNumber, validityStartDate, validityEndDate, dnName, publicKey);
      certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
      
      GeneralNames subjectAltNames = new GeneralNames(new GeneralName[] {new GeneralName(GeneralName.dNSName, dnsName)});
      certBuilder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);
      
//      int usages = KeyUsage.digitalSignature | KeyUsage.keyEncipherment;
//      certBuilder.addExtension(Extension.keyUsage, false, new KeyUsage(usages));
//      
//      KeyPurposeId[] purposes = new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth};
//      ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(purposes);
//      certBuilder.addExtension(Extension.extendedKeyUsage, false, extendedKeyUsage);
      
      String signatureAlgorithm = null;
      
      if (keyAlgorithm.equalsIgnoreCase("EC"))
      {
        signatureAlgorithm = "SHA256withECDSA";
      }
      else if (keyAlgorithm.equalsIgnoreCase("DSA"))
      {
        if (supportsAtLeastJDK7())
        {
          signatureAlgorithm = "SHA256withDSA";
        }
        else
        {
          signatureAlgorithm = "SHA1withDSA";
        }
      }
      else
      {
        signatureAlgorithm = "SHA256withRSA";
      }
      
      AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(signatureAlgorithm);
      AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
      
      ContentSigner signer = null;
      
      if (keyAlgorithm.equalsIgnoreCase("EC"))
      {
        signer = new BcECContentSignerBuilder(sigAlgId, digAlgId).build(privateKey);
      }
      else if (keyAlgorithm.equalsIgnoreCase("DSA"))
      {
        signer = new BcDSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKey);
      }
      else
      {
        signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKey);
      }
      
      return certBuilder.build(signer).getEncoded();
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
  
  public static SSLContext createUnsafeTLSContext()
  {
    try
    {
      TrustManager[] trustAnything = new TrustManager[] {new OverlyOptimisticTrustManager()};
      SSLContext unsafeTLSContext = SSLContext.getInstance("TLS");
      unsafeTLSContext.init(null, trustAnything, new SecureRandom());
      return unsafeTLSContext;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
  
  public static SSLContext createUnsafeTLSContext(PrivateKey privateKey, byte[] certificateData)
  {
    try
    {
      TrustManager[] trustAnything = new TrustManager[] {new OverlyOptimisticTrustManager()};
      KeyManager[] keyManagers = null;
      if (privateKey != null && certificateData != null)
      {
        Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certificateData));
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, "password".toCharArray());
        keyStore.setKeyEntry("entry", privateKey, "password".toCharArray(), new Certificate[] {certificate});
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "password".toCharArray());
        keyManagers = keyManagerFactory.getKeyManagers();
      }
      SSLContext unsafeTLSContext = SSLContext.getInstance("TLS");
      unsafeTLSContext.init(keyManagers, trustAnything, new SecureRandom());
      return unsafeTLSContext;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
  
  public static SSLContext createUnsafeTLSContext(String algorithm, int keySizeBits, String parameterSpec)
  {
    try
    {
      KeyPair keyPair = createKeyPair(algorithm, keySizeBits, parameterSpec);
      if (keyPair != null)
      {
        return createUnsafeTLSContext(keyPair.getPrivate(), createSelfSignedTLSCertificateEncodedData(keyPair));
      }
      else
      {
        return createUnsafeTLSContext();
      }
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
  
  public static SSLSocket createTLSSocket(Socket socket, String host, int port, boolean client, boolean autoClose, SSLContext context)
  {
    try
    {
      return createTLSSocket(socket, host, port, client, autoClose, context.getSocketFactory());
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
  
  public static SSLSocket createTLSSocket(Socket socket, String host, int port, boolean client, boolean autoClose, SSLSocketFactory factory)
  {
    try
    {
      SSLSocket tlsSocket = (SSLSocket) factory.createSocket(socket, host, port, autoClose);
      if (client && supportsAtLeastJDK7() && !supportsAtLeastJDK8())
      {
        tlsSocket.setEnabledProtocols(new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"});
      }
      tlsSocket.setUseClientMode(client);
      return tlsSocket;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
}