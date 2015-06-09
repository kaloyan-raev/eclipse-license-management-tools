package signature;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class GenKeys {

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.out.println("Usage: GenKey nameOfFile");
			System.exit(1);
		}

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(1024, random);

		KeyPair pair = keyGen.generateKeyPair();
		PrivateKey priv = pair.getPrivate();
		PublicKey pub = pair.getPublic();

		Utils.saveKeyToFile(priv, args[0]);
		Utils.saveKeyToFile(pub, args[0] + ".pub");
	}

}
