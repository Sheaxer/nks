/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sheaxe
 */
public class RainbowTable implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private  List<RainbowTableRow> rows;
    
    private final char[][] sets;
    private final BigInteger module;
    
    private final BigInteger[] setSizes;
    
    private BigInteger lastIndex;
    private BigInteger startIndex;
    
    private long t;
    private long m;
    
    
    public BigInteger getModule()
    {
        return this.module;
    }
    
    public void loadRainbowTable(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        System.out.println("Started loading");
        long loadingTimeStart = System.nanoTime();
        //System.out.println("Reading file " + fileName);
        this.rows=null;
        System.gc();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        // index od ktoreho boli hesla generovane
        this.startIndex=(BigInteger)in.readObject();
        //System.out.println("StartIndex " + String.valueOf(this.startIndex));
        // index pokial boli hesla generovalne
        // ak sa budu robit viacere tabulky aby sa generovalo od hodnoty
        this.lastIndex= (BigInteger)in.readObject();
        //System.out.println("End index " + String.valueOf(this.lastIndex));
        // pocet pouzitych redukcnych funkcii
        this.t=in.readLong();
        List<RainbowTableRow> l = (List<RainbowTableRow>) in.readObject();
        /*for(RainbowTableRow r: l)
        {
            System.out.println(r.getPassword() + " - " + Hasher.bigInttoHex(r.getHash()));
        }*/
        this.rows=l;
        long loadingTimeEnd = System.nanoTime();
        double loadingTime = ((double)(loadingTimeEnd - loadingTimeStart)) / 1000000000.0;
        System.out.println("Loaded in " + loadingTime);
        in.close();
    }
    
    public void storeRainbowTable(String fileName) throws FileNotFoundException, IOException
    {
       // System.out.println("Storing table " + fileName);
        ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(fileName)
        );
        out.writeObject(this.startIndex);
       // System.out.println("StartIndex " + String.valueOf(startIndex));
        out.writeObject(this.lastIndex);
       // System.out.println("Ending Index " + String.valueOf(lastIndex));
        out.writeLong(this.t);
       // System.out.println("T is " + String.valueOf(t));
        out.writeObject(this.rows);
       // System.out.println("Rows");
        /*for(RainbowTableRow r: this.rows)
        {
            System.out.println(r.getPassword() + " - " + Hasher.bigInttoHex(r.getHash()));
        }*/
        out.flush();
        out.close();
    }
    
    public RainbowTable(char[][] sets)
    {
        // sety znakov z ktorych mozu pozostavat hesla
        this.sets=sets;
        BigInteger mod = BigInteger.valueOf(sets[0].length);
        this.setSizes = new BigInteger[sets.length];
        setSizes[0] = BigInteger.valueOf(sets[0].length);
        for(int i=1;i<sets.length;i++)
        {
            setSizes[i] = BigInteger.valueOf(sets[i].length);
            mod=mod.multiply(BigInteger.valueOf(sets[i].length));
        }
        this.module=mod;
        this.rows = new ArrayList<>();
        lastIndex=BigInteger.valueOf(0L);
        startIndex=BigInteger.valueOf(0L);
    }
    
    public void order()
    {
        Collections.sort(rows);
    }

    /***
     * Vrati list hesiel ktore postupnou redukciou t redukcnych funkcii a hashovania
     * vedu na hladany hash
     * @param hash hladany hash
     * @return list hesiel
     */
    public List<String> getStartPoint(BigInteger hash)
    {
        int index=Collections.binarySearch(rows, hash);
        if(index<0)
        {
            return null;
        }
        List<String> retList=new ArrayList<>();
        int newIndex=index;
        RainbowTableRow r= this.rows.get(newIndex);
        while(hash.equals(r.getHash()))
        {
           retList.add(r.getPassword());
           newIndex--;
           if(newIndex>=0)
            r= this.rows.get(newIndex);
           else break;
        }
        index++;
        if(index>= this.rows.size())
            return retList;
        r=this.rows.get(index);
        while(hash.equals(r.getHash()))
        {
           retList.add(r.getPassword());
           index++;
           if(index<this.rows.size())
            r= this.rows.get(index);
           else break;
        }
        return retList;
    }
    
    public void addRow(String pass, BigInteger hash)
    {
        this.rows.add(new RainbowTableRow(pass,hash));
    }

    /***
     * Zredukuje hash na heslo za pouzitia a-tej redukcnej funkciie
     * @param a číslo použitej redukčnej funkcie
     * @param hash hash ktorý sa redukuje
     * @return zredukovane heslo
     */
    public String reductionFunction(long a, BigInteger hash)
    {
        BigInteger b = hash.add(BigInteger.valueOf(a)).mod(this.module);
        //System.out.println(b.longValue());
        StringBuilder sb= new StringBuilder();
        //sb.append("ab");
        for(int i=this.sets.length-1;i>=0;i--)
        {
            sb.append(this.sets[i][b.mod(this.setSizes[i]).intValue()]);
            b=b.divide(this.setSizes[i]);
        }
        sb.reverse();
        return (sb.toString());
    }
    
    public String generateRandomPassword()
    {
       StringBuilder sb= new StringBuilder();
       Random generator = new Random();
       for(int i=0;i<sets.length;i++)
       {
           int index=generator.nextInt(sets[i].length);
           sb.append(sets[i][index]);
       }
        //System.out.println("Random password ");
        //System.out.println(sb.toString());
       return sb.toString();
    }

    /***
     * Hladanie hesla k danemu hashu podla schemy NKS 1
     *
     * @param hash
     * @param wholeMessage
     * @return
     */
    public String search(BigInteger hash,String wholeMessage)
    {
       int cores = Runtime.getRuntime().availableProcessors();
        //System.out.println("Cores " + String.valueOf(cores));
       final ExecutorService pool = Executors.newFixedThreadPool(cores);
       final ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(pool);
       for(long i=t;i>0;i--)
       {
           //System.out.println("Searching for " + wholeMessage + " at position " + String.valueOf(i));
           //System.out.println("Submit " + String.valueOf(t));
           completionService.submit(new Searcher(i, hash, wholeMessage));
       }
       String result=null;
       for(long i=0;i<t;i++)
       {
           
            try {
                final Future<String> f=completionService.take();
                String tmpResult = f.get();
                //System.out.println("Service ended");
                if(tmpResult!=null)
                {
                    result = tmpResult;
                    pool.shutdownNow();
                    break;
                }
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(RainbowTable.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
       return result;
    }
    
    private class Searcher implements Callable<String>
    {
        
        private final long startIndex;
        private  BigInteger hash;
        private final String wholeMessage;
        
        public Searcher(long startIndex,BigInteger hash,String wholeMessage)
        {
            this.startIndex=startIndex;
            this.hash=hash;
            this.wholeMessage = wholeMessage;
        }

        /***
         *  kombinacia redukcia hashu a znova hashovanie, od zaciatocneho indexu
         * kontrola ci vysledok je endpoint v rainbowtabulke, ak je  tak od startPointu treba aplikovat
         * redukciu a hashovanie az kym sa nenajde heslo
         * @return najdene heslo alebo null ak heslo nebolo najdene
         * @throws Exception
         */
        @Override
        public String call() throws Exception {
            String password=null;

            for(long i=startIndex;i<t;i++)
            {
                if(Thread.interrupted())
                    return null;
                password = reductionFunction(i, hash);
                hash = NKSCrypto.passwordHash(password);
            }
            if (Thread.interrupted())
            {
                return null;
            }
            
            List<String> possiblePasswords = getStartPoint(hash);
            if(possiblePasswords == null)
                return null;
            boolean found = false;
            /*
             startPoint prejde postupnym hashovanim a redukciami, az do miesta, z ktoreho sme zistili,
             ze hashom a redukciou tohto hesla ziskame end point rainbowtabulky
             */
            for(String s: possiblePasswords)
            {
                password = s;
                for(long i=1;i<startIndex;i++)
                {
                    if(Thread.interrupted())
                        break;
                    
                    hash = NKSCrypto.passwordHash(password);
                    password = reductionFunction(i, hash);
                }
                if (Thread.interrupted())
                    return null;
                String authMessage = NKSCrypto.auth(password);
                assert authMessage != null;
                if(authMessage.equals(wholeMessage))
                {
                    found=true;
                    break;
                }
            }
            if(Thread.interrupted())
                return null;
            if(!found)
                return null;
            return password;
        }
        
    }
    
    public void createTable(BigInteger m, long t)
    {
       this.t = t;
       this.rows = new ArrayList<>();
       this.startIndex=this.lastIndex;
       System.gc();
       int cores = Runtime.getRuntime().availableProcessors();
       ExecutorService service = Executors.newFixedThreadPool(cores);
       List<Future> futures = new ArrayList<>();
       BigInteger index=lastIndex;
       // rozdelenie generacie tabulky na vsetky dostupne threads
       BigInteger addition = m.divide(BigInteger.valueOf(cores));
        //System.out.println("Addition is" + String.valueOf(addition));
       for(int c=0; c<cores-1;c++)
       {
           futures.add(service.submit(new PreComputer(index,index.add(addition))));
           index= index.add(addition);
           //System.out.println("New index is " + String.valueOf(index));
       }
       futures.add(service.submit(new PreComputer(index, index.add(addition).add(m.mod(BigInteger.valueOf(cores))))));
       lastIndex = index.add(addition).add(m.mod(BigInteger.valueOf(cores)));
       for(Iterator<Future> it=futures.listIterator();it.hasNext();)
       {
           Future<List<RainbowTableRow> > f=it.next();
           try {
               this.rows.addAll(f.get());
               it.remove();
               System.gc();
           } catch (InterruptedException | ExecutionException ex) {
               Logger.getLogger(RainbowTable.class.getName()).log(Level.SEVERE, null, ex);
           }
       }
       service.shutdown();
       this.order();
    }
    
    public BigInteger calculateKeySpace()
        {
            BigInteger sum=BigInteger.valueOf(sets[0].length);
            for(int i=1;i<sets.length;i++)
            {
                sum= sum.multiply(BigInteger.valueOf(sets[i].length));
            }
            return sum;
        }
    
    private class PreComputer implements Callable<List<RainbowTableRow> >
    {
        private final  BigInteger startIndex;
        private final BigInteger endIndex;
        
        
        public PreComputer(BigInteger startIndex, BigInteger endIndex)
        {
           this.startIndex = startIndex;
           this.endIndex=endIndex;
            //System.out.println("Start Index is" + String.valueOf(startIndex));
            //System.out.println("End index is" + String.valueOf(endIndex));
        }
        
        
        
        public String generatePassword(int[] indices)
        {
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<indices.length;i++)
            {
                sb.append(sets[i][indices[i]]);
            }
            return sb.toString();
        }

        /***
         * Generacia riadkov rainbowtabulky. Generuju sa mozne hesla ako start pointy, prejde t krat hash hesla + i-ta redukcna funkcia
         * + posledny hash hesla ako end point.
         * @return riadky rainbowtabulky
         * @throws Exception
         */
        @Override
        public List<RainbowTableRow> call() throws Exception 
        {
            // index ktore heslo sa ma generovat
            // kazda pozicia hesla ma mnozinu znakov z ktorej moze pozostavat
            // t.j. kazde mozne heslo ma numericky index
            BigInteger keyIndex = startIndex;
            List<RainbowTableRow> retList = new ArrayList<>();
            int l = sets.length-1;
            int[] indices = new int[sets.length];
            for(int i=l;i>0;i--)
            {
                
                indices[i] = keyIndex.mod(BigInteger.valueOf(sets[i].length)).intValue();
               
                keyIndex = keyIndex.divide(BigInteger.valueOf(sets[i].length));
                
            }
            indices[0] = keyIndex.intValue();

           // kolko hesiel sa ma generovat
            long diff = endIndex.subtract(startIndex).longValue();
            

            for(long i=0;i<diff;i++)
            {
                // generaica hesla posla indexov znakov na poziciach
                String password = generatePassword(indices);

                indices[l]++;
                for(int j=l;j>0;j--)
                {
                    if(indices[j] == sets[j].length)
                    {
                        indices[j]=0;
                        indices[j-1]++;
                    }
                    else
                    {
                        break;
                    }
                }
                // hesla vzniknute hashom hesla + redukciou
                String newPassword = password;
                BigInteger hash;
                for(long k=1;k<t;k++)
                {
                    hash = NKSCrypto.passwordHash(newPassword);
                    newPassword = reductionFunction(k, hash);
                    //System.out.println("Reduced to " + newPassword);
                }
                hash = NKSCrypto.passwordHash(newPassword);
                retList.add(new RainbowTableRow(password, hash));
            }
            return retList;
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    
}
