/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author Tomáš Gono
 *
 * Program sluziaci na prezentaciu Time - Memory - Tradeoff útoku
 * pomocou rainbowtables na Hash
 * Zadanie:
 * Firma sa rozhodla implementovať schému na prihlasovanie s heslom. Využívajú saltovaný hash, ale aby nemuseli riešiť
 * RNG pre salty, tak sa ako salt použije výstup z hash-u generovaného na základe tajného MSK. Schéma je nasledovná:
 *
 * pwd = heslo používateľa, max 16 znakov
 * msk=master key, max. 16 znakov
 * hash1=SHA256(msk|pwd)
 * salt=hex( hash1[0:8] ) - iba z prvých 8 bajtov
 * auth=hex( SHA-256(salt|pwd)[0:8] )
 * Do DB sa uloží salt:auth, prístup je povolený ak hex(SHA-256(salt|pwd')[0:8]) sa zhoduje s auth
 *
 *
 * Uniklo msk (vaše číslené ID v AIS vo forme reťazca, napr. A.B.: '72773') a obsah DB -
 * 100 dvojíc salt:auth. Počas 1 hodiny na cvičení odhaľte čo najviac hesiel.
 * Heslá budú osemznakové v tvare [A-Za-z][a-z]*[0-9]*, z toho prvé 4 sú písmená a zvyšné 4 číslice
 */
public class NKS1 {


    public static final String MSK="0";
   // private static final String PWD = "Abur123";
    private static final int MAGICNUMBER = 26;
    private static List<String> rainbowTableFiles;
    private static int factor = 5;
    //private static final int MAGICNUMBERSKIP=6;
    
    /*
    Structure of config file
    msk - maska suboru, t.j. id studenta - v mojom pripade 81461
    Number of sets -i   - pocet setov znakov z ktorych sa sklada heslo
    next i lines - set symbols split by white space - zoznam znakov z ktorych sa moze skladat heslo
    number of rainbowtables - j - pocet vygenerovanych rainbowtables na lamanie hashu
    next j lines - rainbowtable file path - cesta do suboru, kde su rainbowtable ulozene
    
    
    */

    /***
     *
     * @param args args[0] - cesta ku config súboru
     */
    public static void main(String[] args) 
    {
        File file = new File(args[0]);
        System.out.println("SELECT OPTION: ");
        System.out.println("G - generate rainbow table ");
        System.out.println("L - load rainbow table");
        System.out.println("E - Exit");
        // bude sa generovat tabulka?
        boolean generate=false;
        // je nacitana rainbow tabulka?
        boolean isLoaded=false;
        int numberOfTables=0;
        Scanner s = new Scanner(System.in);
        String a= s.nextLine();
        char[][] sets = null;
        // bola vybrana nejaka moznost?
         boolean chosen=false;
        if(a.equals("G"))
        {
            
            generate=true;
            chosen=false;
            label:
            while(!chosen)
            {
                chosen=true;
                System.out.println("T - test sets"); // testovacie sety s maskou "0" a heslom len 4 cislice
                System.out.println("R - real sets"); // uz skutocne sety
                System.out.println("E - exit");
                a = s.nextLine();
                switch (a) {
                    case "T":
                        sets = generateTestSets();
                        NKSCrypto.setMSK("0");
                        rainbowTableFiles = new ArrayList<>();
                        break;
                    case "R":
                        NKSCrypto.setMSK("81461");
                        rainbowTableFiles = new ArrayList<>();
                        sets = generateSets();
                        break;
                    case "E": // exit
                        chosen = false;
                        break label;
                    default:
                        chosen = false;
                        break;
                }
            }

        }
        else if(a.equals("L")) // nacitaj config a priprava na lamanie hashov
        {
            //System.out.println("GETTING READY TO LOAD FILE");
            //File file = new File(args[0]);
            try {
                // citanie config suboru
                Scanner scanner = new Scanner(file);
                NKSCrypto.setMSK(scanner.nextLine()); // nacitanie MSK
                //System.out.println("SET TO " + NKSCrypto.getMSK());
                int numberOfSets = Integer.parseInt(scanner.nextLine()); // nacitanie setov z ktorych sa budu generovat
                // hesla

                // sety znakov na generovanie hesiel
                sets = new char[numberOfSets][];
                for(int i=0;i<numberOfSets;i++)
                {
                    String line = scanner.nextLine();
                    //rozdelenie podla medzier
                    String[] splitLine = line.split(" ");
                    char[] tmpSet = new char[splitLine.length];
                    for(int j=0; j<splitLine.length;j++)
                    {
                        tmpSet[j] = splitLine[j].charAt(0);
                    }
                    sets[i] = tmpSet;
                }

                //nacitavanie rainbowtables
                 numberOfTables = Integer.parseInt(scanner.nextLine());
                 rainbowTableFiles=new ArrayList<>();
                 for(int i=0;i<numberOfTables;i++)
                 {
                     rainbowTableFiles.add(scanner.nextLine());
                 }
                 /*for(String str: rainbowTableFiles)
                 {
                     System.out.println(str);
                 }*/
                 scanner.close();
                 //isGenerated=true;
                 chosen=true;
                 generate=false;
                 isLoaded = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(NKS1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(chosen)
        {
            RainbowTable r = new RainbowTable(sets);
            if(isLoaded)
            {
                try {
                    // nacitanie prvej rainbowtable
                    r.loadRainbowTable(rainbowTableFiles.get(0));
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(NKS1.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            /*if(isGenerated)
            {
                try {
                    r.loadRainbowTable(rainbowTableFiles.get(0));
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(SKS1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }*/
            while(true)
            {
                // generacia novej rainbow tabulky
                if(generate)
                {
                    // nie je rainbowtabulka nacitana
                    isLoaded=false;
                    // pocet vsetkych moznych hesiel
                    double tmp=r.calculateKeySpace().doubleValue();
                    System.out.println("Key space is " + tmp);
                    // Time memory tradeoff krivka  - N^(2/3) je optimalny pocet
                    double tmp2 = Math.pow(tmp, (2.0/3.0));
                    //tmp2 = tmp2 * ((double) factor);
                    BigInteger roundedTmp2 = BigDecimal.valueOf(tmp2+1.0).toBigInteger();
                    
                    //roundedTmp2 = roundedTmp2.multiply(BigInteger.valueOf(factor));
                    
                    System.out.println("roundedTMP2 is" + roundedTmp2.toString());
                    
                    double t = tmp /  roundedTmp2.doubleValue();
                    long roundedT = Math.round(t);
                    
                    // m*t*t = Hellmanove tabulky - transformacia na Rainbow tabulky
                    // m*t - velkost, t redukcnych funkcii
                    // rountedTmp - velkost tabulky, t.j. pocet riadkov
                    // roundedT - pocet redukcii v kazdom riadku
                    
                    System.out.println("Value of memory space " + roundedTmp2);
                    System.out.println("T is " + roundedT);
                    
                    System.out.println("Total keys generated " + roundedTmp2.multiply(BigInteger.valueOf(roundedT)));
                    //double t=Math.sqrt(tmp2) / (double) factor;
                    System.out.println("Insert name of the rainbowtable file");
                    String tmpName = s.nextLine();
                    // generacia novej rainbowtabulky
                    r.createTable(roundedTmp2,roundedT );   
                    try {
                        // ulozenie rainbowtabulku
                         r.storeRainbowTable(tmpName);
                         rainbowTableFiles.add(tmpName);
                         // zapis do suboru
                         FileWriter fileWriter = new FileWriter(args[0]);
                         PrintWriter printWriter = new PrintWriter(fileWriter);
                         printWriter.write(NKSCrypto.getMSK());
                         printWriter.write("\n");
                         printWriter.write(String.valueOf(sets.length));
                         printWriter.write("\n");
                        for (char[] set : sets) {
                            StringBuilder ttmp = new StringBuilder();
                            for (char cha : set) {
                                ttmp.append(cha);
                                ttmp.append(" ");
                            }
                            printWriter.write(ttmp.toString());
                            printWriter.write("\n");
                        }
                         printWriter.write(String.valueOf(rainbowTableFiles.size()));
                         printWriter.write("\n");
                         for(String name: rainbowTableFiles)
                         {
                             printWriter.write(name);
                             printWriter.write("\n");
                         }
                         printWriter.close();
                         fileWriter.close();
                         
                    } catch (IOException ex) {
                        Logger.getLogger(NKS1.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                 generate=false;
                 System.out.println("SELECT NEXT OPTION");
                 System.out.println("P - find passwords from file");
                 System.out.println("M - make next rainbowtable");
                 System.out.println("G - generate random passwords and try crack them");
                 System.out.println("A - get auth from password");
                 System.out.println("K - get master key");
                 System.out.println("E - exit");
                 a=s.nextLine();
                // System.out.println("I am here hello ");
                 //System.out.println("A is : " + a);
                 if(a.equals("E"))
                 {
                     //System.out.println("WHAT");
                     //break;
                     System.exit(0);
                 }
                 if(a.equals("A"))
                 {

                        System.out.println("Enter password you want auth string for");
                        a=s.nextLine();
                        System.out.println(NKSCrypto.auth(a));
                        continue;

                 }
                 if(a.equals("K"))
                 {
                     System.out.println("Master key is " + NKSCrypto.getMSK());
                     continue;
                 }
                 // generaica dalsej tabulky
                 if(a.equals("M"))
                 {
                     generate=true;
                     continue;
                 }
                 // hladanie heshla k hashu
                 if(a.equals("G") || a.equals("P"))
                 {
                    List<Pair<BigInteger,String>> unknownHashes = null;
                    if(a.equals("P"))
                    {
                        System.out.println("Insert file name ");
                         try {
                             unknownHashes = loadUnknownPasswords(s.nextLine());
                         } catch (FileNotFoundException ex) {
                             Logger.getLogger(NKS1.class.getName()).log(Level.SEVERE, null, ex);
                         }
                    }
                    else
                    {
                        List<String> unknownPasswordsList=new ArrayList<>();
                        for(int i=0;i<100;i++)
                        {
                            String tmpPassord = r.generateRandomPassword();
                            unknownPasswordsList.add(NKSCrypto.auth(tmpPassord));
                        }
                       /* for(String t: unknownPasswordsList)
                        {
                            System.out.println(t);
                        }*/
                        unknownHashes = loadUnknownPasswords(unknownPasswordsList);
                    }
                    
                    // load answer file
                    System.out.println("Going to search");
                    long startTime = System.nanoTime();
                    FileWriter fileWriter;
                    try {
                        fileWriter = new FileWriter(args[1]);
                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        int rainbowTableIndex=0;
                        if(isLoaded)
                        {
                            assert unknownHashes != null;
                            breakUnknownHashes(r, unknownHashes, printWriter);
                            rainbowTableIndex=1;
                        }
                        while(rainbowTableIndex < rainbowTableFiles.size())
                        {
                            System.out.println("Switching");
                            isLoaded=false;
                            String rainbowTableName = rainbowTableFiles.get(rainbowTableIndex);
                            System.out.println("Loading " + rainbowTableName);
                             r.loadRainbowTable(rainbowTableName);
                            assert unknownHashes != null;
                            breakUnknownHashes(r, unknownHashes, printWriter);
                            rainbowTableIndex++;
                        
                        }
                        long endTime = System.nanoTime();
                        long duration = (endTime - startTime);
                        printWriter.close();
                        fileWriter.close();
                        
                        System.out.println("Couldnt find these hashes");
                        assert unknownHashes != null;
                        for(Pair<BigInteger,String> unk: unknownHashes)
                        {
                            System.out.println(unk.getValue());
                        }
                        System.out.println(" ");
                        double secondsTime = ((double)duration) /  1000000000.0;
                        System.out.println("Hashes checked in " + secondsTime + " seconds");
                        
                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(NKS1.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // swap rainbowtables
                    // find hashes
                    
                    
                    // swap rainbowtables
                    
                    
                    
                    // find hashes
                    
                 }
                 
            }
  
        }
        
        
        
        
        
        
       /* System.out.println("Please enter file path");
        generateSets();
        try {
            //String a = NKSCrypto.auth("nbusr123");
            //System.out.println(a);
            
            BigInteger b = NKSCrypto.passwordHash("nbusr123");
            String c = Hasher.bigInttoHex(b);
            System.out.println(c);
            
            
            String r = s.nextLine();
            
            if(c.equals(r))
                System.out.println("same");
            else
                System.out.println("not same");
            
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SKS1.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
    }

    /***
     *
     * @param r rainbowtable
     * @param unknownHashes hashe ku ktorým hľadáme heslá
     * @param printWriter writer ktorý zapíše výsledky do súboru
     */
    private static void breakUnknownHashes(RainbowTable r, List<Pair<BigInteger, String>> unknownHashes, PrintWriter printWriter) {
        for(Iterator<Pair<BigInteger, String> > it = unknownHashes.listIterator(); it.hasNext(); )
        {
             Pair<BigInteger,String> p =it.next();
             String res = r.search(p.getKey(), p.getValue());
             if(res!=null)
             {
                 it.remove();
                 StringBuilder sb=new StringBuilder();
                 sb.append(p.getValue());
                 sb.append(" ");
                 sb.append(res);
                 printWriter.write(sb.toString());
                 printWriter.write("\n");
             }
             /*else
             {
                 System.out.println("I CANT FIND IT");
                 System.out.println(p.getValue());
             }*/
        }
    }


    public static long calculateKeySpace(char[][] sets)
    {
        long sum=sets[0].length;
        //System.out.println(sets[0].length);
        for(int i=1;i<sets.length;i++)
        {
            sum= sum * sets[i].length;
        }
        //System.out.println(sum);
        return sum;
    }
    
    public static char[][] generateSets()
    {
        char[] set1 = generateSet('A', MAGICNUMBER);
        char[] set2 = generateSet('a',MAGICNUMBER);
        char[] set12=new char[set1.length+set2.length];
        System.arraycopy(set1, 0, set12, 0, set1.length);
        int j= set1.length;
        for (char c : set2) {
            set12[j] = c;
            j++;
        }
        char[] set3 = generateSet('0',10);
        char[][] sets = new char[8][];
        sets[0] = set12;
        
        for(int i=1;i<4;i++)
        {
            sets[i] = set2;
            
        }
        for(int i=4;i<8;i++)
        {
            sets[i] = set3;
            //System.out.println(sets[i][3]);
        }
        
        /*for(int i=0;i<sets.length;i++)
        {
            System.out.println(sets[i].length);
            for(int k=0; k<sets[i].length; k++)
            {
                System.out.print(sets[i][k]);
                System.out.print(" ");
            }
            System.out.println("");
        }*/
        return sets;
    }
    
    public static char[][] generateTestSets()
    {
        char[][] sets=new char[4][];
        char[] set=generateSet('0',10);
        for(int i=0;i<4;i++)
        {
            sets[i]=set;
        }
        return sets;
    }
    
    public static List<Pair<BigInteger,String> > loadUnknownPasswords(String fileName) throws FileNotFoundException
    {
       List<Pair<BigInteger,String> > retList = new ArrayList<>();
       Scanner scanner = new Scanner(new File(fileName));
       while(scanner.hasNext())
       {
           String str=scanner.nextLine();
           byte[] byteStr=Hasher.hexStringToByteArray(str.split(":")[0]);
           Pair<BigInteger,String> p = new Pair<>(new BigInteger(byteStr),str);
           retList.add(p);
       }
       return retList;
    }
    
    public static List<Pair<BigInteger,String> > loadUnknownPasswords(List<String> hashList)
    {
        List<Pair<BigInteger,String> > retList = new ArrayList<>();
        for(String s: hashList)
        {
            byte[] byteStr=Hasher.hexStringToByteArray(s.split(":")[0]);
            Pair<BigInteger,String> p = new Pair<>(new BigInteger(byteStr),s);
            retList.add(p);
        }
        return retList;
    }
     
    
    
    
    
    
    private static char[] generateSet(char start, int howMany)
    {
        char[] set=new char[howMany];
        set[0]=start;
        for(int i=1; i<howMany;i++ )
        {
            start++;
            set[i]= start;
        }
        return set;
    }
    
    
}
