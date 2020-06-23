/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nks;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Trieda uchovavajuca jeden riadok rainbowtabulky,
 * heslo ako start point a hash ako endpoint,
 * na start point je aplikovane hashovanie + i-ta redukcna funkcia t krat, + posledny hash a tak ziskame end point.
 * @author Sheaxe
 */
public class RainbowTableRow implements Comparable<Object>, Serializable{
    
    private String password;
    private BigInteger hash;
    private static final long serialVersionUID = 1L;
    
    
    @Override
    public int compareTo(Object o) {
        if(o instanceof BigInteger)
        {
            return this.hash.compareTo((BigInteger)o);
        }
        else if(o instanceof RainbowTableRow)
        {
            return this.hash.compareTo(((RainbowTableRow) o).hash);
        }
        return 0;
    }
    
    public RainbowTableRow(String password, BigInteger hash)
    {
        this.password=password;
        this.hash=hash;
        
    }
    
    public RainbowTableRow()
    {
        
    }
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigInteger getHash() {
        return hash;
    }

    public void setHash(BigInteger hash) {
        this.hash = hash;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.password);
        hash = 97 * hash + Objects.hashCode(this.hash);
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
        final RainbowTableRow other = (RainbowTableRow) obj;
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        return Objects.equals(this.hash, other.hash);
    }
    
    
    
    
    
}
