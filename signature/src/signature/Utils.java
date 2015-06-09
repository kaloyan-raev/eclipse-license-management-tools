/*******************************************************************************
 * Copyright (c) 2015 Kaloyan Raev.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kaloyan Raev - initial API and implementation
 *******************************************************************************/
package signature;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Utils {

	public static void saveKeyToFile(Key key, String fileName)
			throws IOException {
		byte[] encoded = key.getEncoded();
		FileOutputStream keyfos = new FileOutputStream(fileName);
		keyfos.write(encoded);
		keyfos.close();
	}

	public static PublicKey readPublicKeyFromFile(String fileName)
			throws IOException, GeneralSecurityException {
		byte[] encoded = readFile(fileName);

		return readPublicKeyFromBytes(encoded);
	}

	public static PublicKey readPublicKeyFromBytes(byte[] bytes)
			throws GeneralSecurityException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
		PublicKey key = keyFactory.generatePublic(keySpec);

		return key;
	}

	public static PrivateKey readPrivateKeyFromFile(String fileName)
			throws IOException, GeneralSecurityException {
		byte[] encoded = readFile(fileName);

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
		PrivateKey key = keyFactory.generatePrivate(keySpec);

		return key;
	}

	public static byte[] readFile(String fileName) throws IOException {
		FileInputStream in = new FileInputStream(fileName);
		byte[] bytes = new byte[in.available()];
		in.read(bytes);

		in.close();

		return bytes;
	}

}
