/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nks;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sheaxe
 */
public class NKSCrypto {
    
    private static  String MSK="81461";
    
    public static void setMSK(String s)
    {
        MSK = s;
    }
    
    
    public static String getMSK()
    {
        return MSK;
    }

    /***
     * Schema hashovania zo zadania NKS 1
     * @param password heslo, ktoré sa hashuje
     * @return - hash
     */
    public static String auth(String password)
    {
        try {


            byte[] hash1=Hasher.byteHash(MSK, password);
            String salt = Hasher.bytesToHex(Arrays.copyOfRange(hash1,0,8));
            byte[] dget = Hasher.byteHash(salt, password);
            StringBuilder sb = new StringBuilder();
            sb.append(salt);
            sb.append(':');
            sb.append(Hasher.bytesToHex(Arrays.copyOfRange(dget,0,8)));
            String result = sb.toString();
            //System.out.println(result);
            return result;
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(NKS1.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static BigInteger passwordHash(String password) throws NoSuchAlgorithmException
    {
        byte[] tmp = Hasher.byteHash(MSK, password);
        return(new BigInteger(Arrays.copyOfRange(tmp,0,8))); 
    }
    
    public static String auth(String password, BigInteger hash) throws NoSuchAlgorithmException
    {
        String salt = Hasher.bigInttoHex(hash);
        StringBuilder sb=new StringBuilder();
        sb.append(salt);
        sb.append(":");
        
        sb.append(Hasher.hexHash(salt, password));
        return sb.toString();
    }
}
