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
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;

public class SignLicense {

	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.out.println("Usage: SignLicense licenseFile privateKey");
			System.exit(1);
		}

		Properties license = new Properties();
		license.load(new FileInputStream(args[0]));

		PrivateKey privateKey = Utils.readPrivateKeyFromFile(args[1]);
		Signature signature = Signature.getInstance("SHA1withDSA", "SUN");
		signature.initSign(privateKey);

		String[] propKeys = license.keySet().toArray(new String[0]);
		Arrays.sort(propKeys);
		for (String propKey : propKeys) {
			if (!"Signature".equals(propKey)) {
				String propValue = license.getProperty(propKey);
				signature.update(propValue.getBytes("UTF-8"));
			}
		}

		byte[] sig = signature.sign();
		license.setProperty("Signature", new String(Base64.encodeBase64(sig)));
		license.store(new FileOutputStream(args[0]), null);
	}

}
