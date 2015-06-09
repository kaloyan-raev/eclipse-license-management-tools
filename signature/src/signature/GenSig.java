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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.Signature;

public class GenSig {

	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.out.println("Usage: GenSig nameOfFileToSign");
			System.exit(1);
		}

		PrivateKey priv = Utils.readPrivateKeyFromFile(args[1]);

		Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
		sig.initSign(priv);

		FileInputStream fis = new FileInputStream(args[0]);
		BufferedInputStream bufin = new BufferedInputStream(fis);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = bufin.read(buffer)) >= 0) {
			sig.update(buffer, 0, len);
		}
		bufin.close();

		byte[] realSig = sig.sign();

		/* save the signature in a file */
		FileOutputStream sigfos = new FileOutputStream("sig");
		sigfos.write(realSig);
		sigfos.close();
	}

}
