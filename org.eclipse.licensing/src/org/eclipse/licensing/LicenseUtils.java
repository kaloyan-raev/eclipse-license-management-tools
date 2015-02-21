package org.eclipse.licensing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

public class LicenseUtils {
	
	public static boolean hasValidLicense(UUID productId, byte[] encodedPublicKey) {
		PublicKey publicKey;
		try {
			publicKey = readPublicKeyFromBytes(encodedPublicKey);
		} catch (NoSuchAlgorithmException | NoSuchProviderException
				| InvalidKeySpecException e) {
			e.printStackTrace();
			return false;
		}
		
		File licensesFolder = new File("/home/raev/Work/Licensing");
		for (File file : licensesFolder.listFiles()) {
			if (isLicenseFile(file) && productId.equals(getLicenseProductId(file)) && isAuthentic(file, publicKey)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isLicenseFile(File file) {
		return file != null && file.isFile() && file.getName().endsWith(".license");
	}

	private static UUID getLicenseProductId(File file) {
		if (file == null)
			return null;
		
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			return UUID.fromString(props.getProperty("ProductId"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static PublicKey readPublicKeyFromBytes(byte[] bytes) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
		PublicKey key = keyFactory.generatePublic(keySpec);

		return key;
	}

	private static boolean isAuthentic(File file, PublicKey publicKey) {
		String signatureFileName = file.getName().substring(0, file.getName().length() - 8) + ".signature";
		byte[] sigToVerify;
		try {
			sigToVerify = IOUtils.toByteArray(new FileInputStream(new File(file.getParentFile(), signatureFileName)));
		} catch (IOException e) {
			// no signature file?
			e.printStackTrace();
			return false;
		}
		
		Signature sig;
		try {
			sig = Signature.getInstance("SHA1withDSA", "SUN");
			sig.initVerify(publicKey);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
			// invalid public key?
			e.printStackTrace();
			return false;
		}
		
		try {
			sig.update(IOUtils.toByteArray(new FileInputStream(file)));
		} catch (SignatureException | IOException e) {
			// no license file?
			e.printStackTrace();
			return false;
		}
		
		try {
			return sig.verify(sigToVerify);
		} catch (SignatureException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
