/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nks;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Sheaxe
 */
public class PasswdHash implements Comparable<PasswdHash> {
    
    private final String passwd;
    private final BigInteger hash;
    
    public BigInteger getHash()
    {
        return this.hash;
    }
    
    public String getPasswd()
    {
        return this.passwd;
    }
    
    public PasswdHash(String passwd) throws NoSuchAlgorithmException
    {
        this.passwd = passwd;
        byte[] tmp = Hasher.byteHash(NKS1.MSK, passwd);
        hash = new BigInteger(Arrays.copyOfRange(tmp,0,8));
    }
    
    public PasswdHash(String passwd, BigInteger hash)
    {
      this.passwd = passwd;
      this.hash = hash;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.hash);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PasswdHash other = (PasswdHash) obj;
        return Objects.equals(this.hash, other.hash);
    }

    
    

    @Override
    public String toString() {
        StringBuilder sb= new StringBuilder();
        sb.append(passwd);
        sb.append(':');
        sb.append(hash);
        return sb.toString();
    }
    

    
    
    
    
    @Override
    public int compareTo(PasswdHash o) {
        return(this.hash.compareTo(o.hash));
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
