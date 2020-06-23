/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nks;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Sheaxe
 */
public class Hasher {
    
    
    public static String hexHash(String salt, String message) throws NoSuchAlgorithmException
    {
        
        return bytesToHex(byteHash(salt, message));
    }
    
    public static BigInteger bigIntHash(String salt, String message) throws NoSuchAlgorithmException
    {
        byte[] encodedhash = byteHash(salt, message);
        return new BigInteger(encodedhash);
    }
    
    public static String bigInttoHex(BigInteger b)
    {
        return bytesToHex(b.toByteArray());
    }
    
    public static byte[] byteHash(String salt, String message) throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        StringBuilder sb = new StringBuilder();
        sb.append(salt);
        sb.append(message);
        return digest.digest(
            sb.toString().getBytes(StandardCharsets.UTF_8));
    }
    
    public static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder();
    for (int i = 0; i < hash.length; i++) 
    {
        String hex = Integer.toHexString(0xff & hash[i]);
        if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
    }
    return hexString.toString();
}
    
    public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                             + Character.digit(s.charAt(i+1), 16));
    }
    return data;
}
    
}
