package org.vate.network.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLVerificationDisabler
{
	private static class OverlyOptimisticHostnameVerifier implements HostnameVerifier
	{
		public boolean verify(String arg0, SSLSession arg1)
		{
			return true;
		}
	}
	
	private static class PublicKeyInfrastructureRejector implements TrustManager, X509TrustManager
	{
		public java.security.cert.X509Certificate[] getAcceptedIssuers()
		{
			return new java.security.cert.X509Certificate[] {};
		}
		
		@SuppressWarnings("all")
		public boolean isServerTrusted(java.security.cert.X509Certificate[] certs)
		{
			return true;
		}
		
		@SuppressWarnings("all")
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
	
	public static boolean install()
	{
		try
		{
			TrustManager[] trustAnything = new TrustManager[] { new PublicKeyInfrastructureRejector() };
			SSLContext unverifiedSSL = SSLContext.getInstance("SSL");
			unverifiedSSL.init(null, trustAnything, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(unverifiedSSL.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new OverlyOptimisticHostnameVerifier());
			try
			{
				SSLContext.setDefault(unverifiedSSL);
			}
			catch (Throwable ei)
			{
				return false;
			}
		}
		catch (Throwable e)
		{
			return false;
		}
		return true;
	}
}