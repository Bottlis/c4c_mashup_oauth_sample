package com.c4e.oauth;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthInfoProcessor {
	private static final Logger LOG = Logger
			.getLogger(AuthInfoProcessor.class.getName());
	
	private static final String CA_CERT = "-----BEGIN CERTIFICATE-----\n" + "MIICZjCCAc+gAwIBAgIECAAAATANBgkqhkiG9w0BAQUFADBFMQswCQYDVQQGEwJE\n"
			+ "RTEcMBoGA1UEChMTU0FQIFRydXN0IENvbW11bml0eTEYMBYGA1UEAxMPU0FQIFBh\n"
			+ "c3Nwb3J0IENBMB4XDTAwMDcxODEwMDAwMFoXDTIxMDQwMTEwMDAwMFowRTELMAkG\n"
			+ "A1UEBhMCREUxHDAaBgNVBAoTE1NBUCBUcnVzdCBDb21tdW5pdHkxGDAWBgNVBAMT\n"
			+ "D1NBUCBQYXNzcG9ydCBDQTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA/2rT\n"
			+ "TxBHa450XQCJ/ENotmAwKpFdyKWdU7KC4p8X0VEz/DB4Zu5Digq91f9wxsAYyvbh\n"
			+ "hvoZ5nZimr1sFiWw60gCryDI2qINowZX/sWmYGqguVyBrTxjjEwnAYQXno53RFR5\n"
			+ "p0Aa9RLfLNSITWeHeELKT5ahpGckGdrh4R6+vSMCAwEAAaNjMGEwDwYDVR0TAQH/\n"
			+ "BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAfYwHQYDVR0OBBYEFBpERaZXcXBWARx4JHpR\n"
			+ "NkzANdBeMB8GA1UdIwQYMBaAFBpERaZXcXBWARx4JHpRNkzANdBeMA0GCSqGSIb3\n"
			+ "DQEBBQUAA4GBACwoEOHrYBA0pt7ClKyLfO2o2aJ1DyGCkzrM7RhStTE1yfCpiagc\n"
			+ "XUYu4yCM1i7jPnAWkpMe1NhpwEEbiKPAa3jLJ7iIXN3e/qZG0HAyPOQS3KdAQsiC\n"
			+ "bL9ysfX0LqKir68z0Tv0SYtJTMnPfkCtGXt+D75wWSY7dyI0Xu7Yl9kH\n" + "-----END CERTIFICATE-----";
	
	private AuthInfo authInfo = null;
	
	public AuthInfoProcessor(String payload, String signature){
		Map<String, String> info = new HashMap<String, String>();
		TypeReference<HashMap<String,String>> typeRef = new TypeReference<HashMap<String,String>>() {};
		try {
			info = new ObjectMapper().readValue(payload, typeRef);
		} catch (JsonParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		info.put("json_payload", payload);
		info.put("x-c4c-signature", signature);					
		
		boolean verifySignature = false;
		try {
			verifySignature = this.verifySignature(info);
			if(verifySignature){
				this.authInfo = parseJsonPayload(payload);		
			}
		} catch (InvalidKeyException | Base64DecodingException | CertificateException | NoSuchAlgorithmException
				| SignatureException | IOException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		}
		LOG.info("authInfo verification : " + verifySignature);
	}
	
	public String getUser(){
		if(authInfo == null){
			return null;
		}
		return authInfo.getUser();
	}
	
	public String getHost(){
		if(authInfo == null){
			return null;
		}
		return authInfo.getHost();
	}
	
	private AuthInfo parseJsonPayload(String jsonPayload){
		AuthInfo authInfo = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			authInfo = mapper.readValue(jsonPayload, AuthInfo.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
			LOG.severe(e.getMessage());
		} catch (JsonMappingException e) {
			e.printStackTrace();
			LOG.severe(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			LOG.severe(e.getMessage());
		}
		return authInfo;
	}
	

	private boolean verifySignature(Map<String, String>authInfo) throws Base64DecodingException, CertificateException, IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		String signingCertificate = getSigningCertificate(authInfo);
		boolean isValidCert = isCertIssuedByCa(signingCertificate);
		
		if (isValidCert) {
			PublicKey publicKey = getPublicKey(signingCertificate);
			
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(publicKey);
			signature.update(authInfo.get("json_payload").getBytes());
			return signature.verify(getSignature(authInfo));
		} else {
			LOG.warning("Cert verification failed");		
		}		
		return false;		
	}
	
	private byte[] getSignature(Map<String, String> payload) throws Base64DecodingException {
		byte[] result = Base64.decode(payload.get("x-c4c-signature").getBytes());
		return result;
	}
	
	private String getSigningCertificate(Map<String, String> payload) throws IOException {
		
		String certBody = payload.get("base64_cert");
		
		return formatCertificate(certBody);
	}
	
	private PublicKey getPublicKey(String cert) throws IOException, CertificateException {

		CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
		Certificate certificate = certificateFactory.generateCertificate(IOUtils.toInputStream(cert));

		PublicKey publicKey = certificate.getPublicKey();
		return publicKey;
	}	
	
	private boolean isCertIssuedByCa(String base64Cert) throws CertificateException {
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
		Certificate caCert = certificateFactory.generateCertificate(IOUtils.toInputStream(CA_CERT));

		Certificate tenantCert = certificateFactory.generateCertificate(IOUtils.toInputStream(base64Cert));

		try { 
			tenantCert.verify(caCert.getPublicKey());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}

	private String formatCertificate(String certBody) throws IOException {
		
		String header = "-----BEGIN CERTIFICATE-----\n";
		String footer = "\n-----END CERTIFICATE-----\n";

		byte[] allBytes = IOUtils.toByteArray(IOUtils.toInputStream(certBody));
		for (int i = 0; i < allBytes.length; i++) {
			if (allBytes[i] == '\\') {
				allBytes[i++] = 10;
				allBytes[i++] = 10;
			}
		}

		String body = new String(allBytes);

		String result = header + body + footer;

		System.out.println(result);

		return result;
		
	}
}