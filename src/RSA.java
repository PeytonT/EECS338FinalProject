import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigInteger;
import java.security.SecureRandom;

// Adapted from an RSA implementation at http://www.java2s.com/Code/Java/Security/SimpleRSApublickeyencryptionalgorithmimplementation.htm
public class RSA {
  // Modulus, Private Key, and Public Key for the RSA encryption algorithm
  private BigInteger mod, priv, pub;

  private int bitlen = 1024;

  // Create an instance that can encrypt using someone elses public key.
  public RSA(BigInteger mod, BigInteger pub) {
    this.mod = mod;
    this.pub = pub;
  }

  // Create an instance that can encrypt and decrypt using previously generated keys.
    public RSA(BigInteger mod, BigInteger pub, BigInteger priv) {
    this.mod = mod;
    this.pub = pub;
    this.priv = priv;
    }

  // Create an instance that can both encrypt and decrypt.
  public RSA(int bits) {
    bitlen = bits;
    SecureRandom r = new SecureRandom();
    BigInteger p = new BigInteger(bitlen / 2, 100, r);
    BigInteger q = new BigInteger(bitlen / 2, 100, r);
    mod = p.multiply(q);
    BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
    pub = new BigInteger("3");
    while (m.gcd(pub).intValue() > 1) {
      pub = pub.add(new BigInteger("2"));
    }
    priv = pub.modInverse(m);
  }

  // Encrypt the given plaintext message. 
  public synchronized String encrypt(String message) {
    return (new BigInteger(message.getBytes())).modPow(pub, mod).toString();
  }

  // Encrypt the given plaintext message. 
  public synchronized BigInteger encrypt(BigInteger message) {
    return message.modPow(pub, mod);
  }

  // Decrypt the given ciphertext message. 
  public synchronized String decrypt(String message) {
    return new String((new BigInteger(message)).modPow(priv, mod).toByteArray());
  }

  // Decrypt the given ciphertext message. 
  public synchronized BigInteger decrypt(BigInteger message) {
    return message.modPow(priv, mod);
  }

  // Return the modulus.
  public synchronized BigInteger get_mod() {
    return mod;
  }

  // Return the public key.
  public synchronized BigInteger get_pub() {
    return pub;
  }

  // Return the private key. 
  public synchronized BigInteger get_priv() {
    return priv;
  }

  // Creates an RSA file containing the modulus, public key, and private key for a username specified as the first and only argument
  // For the username 'user' the resulting file is user_RSA, saved in the location in which the file is run
  public static void main(String[] args) {

    // Create an RSA object of 1024 bits
    RSA rsa = new RSA(1024);
    // Parse the username
    String username = args[0];
    PrintWriter pw = null;

    try {
      // Create the file to write to
      File file = new File(username + "_RSA");
      FileWriter fw = new FileWriter(file, true);
      pw = new PrintWriter(fw);
      // Write the values to the file
      pw.println(rsa.get_mod().toString());
      pw.println(rsa.get_pub().toString());
      pw.println(rsa.get_priv().toString());
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (pw != null) {
        pw.close();
      }
    }
  }
}