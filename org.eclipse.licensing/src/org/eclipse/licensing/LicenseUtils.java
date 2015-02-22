package org.eclipse.licensing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;
import java.util.UUID;

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
		try {
			License license = new License(file);
			return license.isValid(publicKey);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
