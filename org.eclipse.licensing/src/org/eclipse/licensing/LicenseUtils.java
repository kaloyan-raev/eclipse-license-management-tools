package org.eclipse.licensing;

import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
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
			if (file.isFile()) {
				License license = new License(file);
				if (productId.equals(license.getProductId()) && license.verifySignature(publicKey)) {
					return true;
				}
			}
		}
		return false;
	}

	public static PublicKey readPublicKeyFromBytes(byte[] bytes) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
		PublicKey key = keyFactory.generatePublic(keySpec);

		return key;
	}

}
