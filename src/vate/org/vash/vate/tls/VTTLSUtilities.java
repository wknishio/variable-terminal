package org.vash.vate.tls;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;
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
import org.vash.vate.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.vash.vate.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.vash.vate.org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.vash.vate.org.bouncycastle.crypto.util.PublicKeyFactory;
import org.vash.vate.org.bouncycastle.operator.ContentSigner;
import org.vash.vate.org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.vash.vate.org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.vash.vate.org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.vash.vate.org.infinispan.server.resp.commands.string.XXH3;
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
  
  public static void allowUnsafeCipherSuites()
  {
    try
    {
      java.security.Security.setProperty("jdk.certpath.disabledAlgorithms", "");
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "");
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      java.security.Security.setProperty("jdk.security.legacyAlgorithms", "");
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      java.security.Security.setProperty("jdk.crypto.disabledAlgorithms", "");
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public static boolean disableHttpsTLSVerifications()
  {
    try
    {
      System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
      System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
      System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
      //System.setProperty("jdk.tls.disabledAlgorithms", "");
      //System.setProperty("jdk.certpath.disabledAlgorithms", "");
      //System.setProperty("jsse.enableSNIExtension", "false");
      
      //System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
      TrustManager[] trustAnything = new TrustManager[]
      { new OverlyOptimisticTrustManager() };
      //KeyManager[] manageNothing = new KeyManager[] { new OverlyOptimisticKeyManager() };
      SSLContext unverifiedTLS = SSLContext.getInstance("TLS");
      unverifiedTLS.init(null, trustAnything, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(unverifiedTLS.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier(new OverlyOptimisticHostnameVerifier());
      try
      {
        Method setDefaultMethod = SSLContext.class.getDeclaredMethod("setDefault", SSLContext.class);
        setDefaultMethod.invoke(null, unverifiedTLS);
        // SSLContext.setDefault(unverifiedSSL);
      }
      catch (Throwable ei)
      {
        //ei.printStackTrace();
        // return false;
      }
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
      return false;
    }
    return true;
  }
  
  public static boolean supportsAtLeastJDK6()
  {
    return VTReflectionUtils.getJavaVersion() >= 6;
  }
  
  public static boolean supportsAtLeastJDK8()
  {
    return VTReflectionUtils.getJavaVersion() >= 8;
  }
  
  public static KeyPair generateKeyPair(String algorithm, int keySizeBits) throws Throwable
  {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
    if (supportsAtLeastJDK6())
    {
      keyGen.initialize(keySizeBits);
    }
    else
    {
      keyGen.initialize(2048);
    }
    KeyPair keyPair = keyGen.generateKeyPair();
    return keyPair;
  }
  
  public static SSLContext createOptimisticTLSClientContext() throws Throwable
  {
    TrustManager[] trustAnything = new TrustManager[]
    { new OverlyOptimisticTrustManager() };
    SSLContext unverifiedTLS = SSLContext.getInstance("TLS");
    unverifiedTLS.init(null, trustAnything, new SecureRandom());
    return unverifiedTLS;
  }
  
  public static SSLContext createUnsafeTLSServerContext(KeyPair keyPair, Random random) throws Throwable
  {
    PrivateKey privateKey = keyPair.getPrivate();
    PublicKey publicKey = keyPair.getPublic();
    Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(createSelfSignedTLSCertificateData(publicKey.getEncoded(), privateKey.getEncoded(), random)));
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(null, "password".toCharArray());
    //keyStore.setCertificateEntry("server", certificate);
    keyStore.setKeyEntry("server", privateKey, "password".toCharArray(), new Certificate[] {certificate});
    //TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    //trustManagerFactory.init(keyStore);
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, "password".toCharArray());
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
    return sslContext;
  }
  
  private static byte[] createSelfSignedTLSCertificateData(byte[] publicKey, byte[] privateKey, Random random) throws Throwable
  {
    String commonName = UUID.randomUUID().toString();
    
    AsymmetricKeyParameter publicKeyParameter = PublicKeyFactory.createKey(publicKey);
    AsymmetricKeyParameter privateKeyParameter = PrivateKeyFactory.createKey(privateKey);
    
    AsymmetricCipherKeyPair keyPair = new AsymmetricCipherKeyPair(publicKeyParameter, privateKeyParameter);
    
    X500Name dnName = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, commonName).addRDN(BCStyle.O, UUID.randomUUID().toString()).addRDN(BCStyle.C, "US").build();
    
    long now = System.currentTimeMillis();
    Date validityStartDate = new Date(now - (30L * 24 * 60 * 60 * 1000));
    Date validityEndDate = new Date(now + (30L * 24 * 60 * 60 * 1000));
    
    BigInteger serialNumber = BigInteger.valueOf(random.nextLong());
    X509v3CertificateBuilder certBuilder = new BcX509v3CertificateBuilder(dnName, serialNumber, validityStartDate, validityEndDate, dnName, keyPair.getPublic());
    
    certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
    
    GeneralNames subjectAltNames = new GeneralNames(new GeneralName[]
    {
      new GeneralName(GeneralName.dNSName, commonName),
      //new GeneralName(GeneralName.iPAddress, "127.0.0.1")
    });
    
    certBuilder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);
    
    AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
    AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
    
    ContentSigner signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(keyPair.getPrivate());
    
    return certBuilder.build(signer).getEncoded();
  }
  
  public static boolean verifyCustomTLSNegotiation(Socket socket, String host, int port, KeyPair keyPair, Random random, boolean client)
  {
    try
    {
      byte[] uuid = UUID.randomUUID().toString().getBytes();
      byte[] padding = null;
      byte[] data = null;
      padding = new byte[(int)(XXH3.hash64(uuid) & 0x0FFF)];
      random.nextBytes(padding);
      data = new byte[uuid.length + padding.length];
      System.arraycopy(uuid, 0, data, 0, uuid.length);
      System.arraycopy(padding, 0, data, uuid.length, padding.length);
      SSLSocketFactory factory = null;
      if (client)
      {
        factory = createOptimisticTLSClientContext().getSocketFactory();
      }
      else
      {
        factory = createUnsafeTLSServerContext(keyPair, random).getSocketFactory();
      }
      SSLSocket tls = (SSLSocket) factory.createSocket(socket, host, port, false);
      tls.setUseClientMode(client);
      DataOutputStream output = new DataOutputStream(tls.getOutputStream());
      DataInputStream input = new DataInputStream(tls.getInputStream());
      output.write(data);
      output.flush();
      input.readFully(uuid);
      padding = new byte[(int)(XXH3.hash64(uuid) & 0x0FFF)];
      input.readFully(padding);
      return true;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
      try
      {
        socket.close();
      }
      catch (Throwable e)
      {
        
      }
    }
    return false;
  }
}