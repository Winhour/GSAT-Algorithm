package projekt.gsat;
import java.util.*;
import org.apache.commons.jexl2.*; // używamy biblioteki jexl2, potrzebne biblioteki: commons-jexl-2.1.1.jar i commons-logging-1.2.jar


/**
 *
 * @author Marcin
 */

public class ProjektGSAT {

    static int HowManyRestarts = 19;        //ile razy chcemy dodatkowo randomizować w przypadku nie znalezienia spełnienia
    static boolean showAlgorithm = false;   //zmień w zależności czy chcesz zobaczyć działanie programu
    
    public static void main(String[] args) {
        
        
        
       /*Podawana formuła powinna wyglądać następująco: 
       podformuła(1) && podformuła(2) && ... podformuła(n)
        
       Przykładowe formuły: 
       a && !b && c && !d
       a && !b && c && !d && e && !f 
       (a || b) && (c || !d)
       a && !a
       (a && (!c || b)) && (!b || (e && d)) 
       (a || b || c) && !a && !b && !c
       a && (!a || b) && (!a || c)  
       a && (!a || b) && (!a || c) && (!b || c) && d
       a && (!a || b) && (!a || c) && (!b || c) && !d && (d || (a && c)) && e && !f && (!e || g)  
       */
        
        
        //Pobranie formuły od użytkownika
        
        System.out.println("Podaj formułę logiczną:");
        Scanner sc = new Scanner(System.in);
        String exp = sc.nextLine();                                     //pobieramy formułę od użytkownika w stringu, zmienne powinny być podane jako char-y
        System.out.println();
        
        
        //Wyszukiwanie zmiennych z formuły
        
        ArrayList listaZM = new ArrayList();                            //tworzymy listę w której umieścimy zmienne z formuły
        
        for (int i = 0; i < exp.length(); i++){                         //dodawanie zmiennych do listy
            char c = exp.charAt(i);   
            if ((listaZM.contains(c) == false) && c != ' ' && c != '(' && c != ')' && c != '!' && c != '&' && c != '|'){
                listaZM.add(c);
            }
        }
        
        System.out.println("Zmienne: " + Arrays.toString(listaZM.toArray()));
        System.out.println();
        
        
        //Rozbicie na podformuły
        
        
        ArrayList listaPod = new ArrayList();   
        int nawiasCounter = 0;
        String tmpS = "";
        
        for (int i = 0; i < exp.length(); i++){  
            
            char c = exp.charAt(i); 
            
            if ((c == '&' && nawiasCounter == 0) || i==(exp.length()-1)){   //spawdza czy doszliśmy do końca danej formuły
                if (i==(exp.length()-1)) tmpS +=c;
                listaPod.add(tmpS);
                tmpS = "";
                i+=2;                                               //przeskakujemy dodatkowe '&' i spację
                continue;
            }
            
            tmpS += c;
            
            if (c == '(') nawiasCounter++;
            
            if (c == ')') nawiasCounter--;
            
            
                
        }
        
        System.out.println("Podformuły: " + Arrays.toString(listaPod.toArray()));
        System.out.println();
        
  
        
        JexlEngine jexl = new JexlEngine();                 // inicjalizacja Jexl
        
        jexl.setSilent(true);                               //przy takim ustawieniu nulle są przestawione na false
        jexl.setLenient(true);
        
        ArrayList listaExp = new ArrayList();

        for (Object s: listaPod ){
            String temp = "" + s;
            Expression expression = jexl.createExpression(temp);    //przekształca string w wyrażenie logiczne
            listaExp.add(expression);
        }
        
        
        JexlContext jexlContext = new MapContext();         //tworzy kontekst dla formuły
        
 

        
        
        //Algorytm GSAT
        
        
        
        
        
        if (true == showAlgorithm){                          //wersja z wypisywaniem działania
        
        Random rand = new Random();
        boolean [] r = {true, false};
        
        for (Object l : listaZM) {                          //dajemy losowe wartości dla wszystkich zmiennych
            String temp = "" + l;
            boolean rndB = r[rand.nextInt(2)];
            jexlContext.set(temp, rndB);                    
        }
        
        wypiszZmienne(listaZM, jexlContext);
        
        int SAT;                                        //SAT - ile podformuł jest spełnionych
        int tSAT = 0;                                       //tSAT - tu przechowujemy informacje o tym czy po zmianie zmiennej zwiększył się SAT
        int RestartFlag = 0;                                //kontrolujemy ile randomizacji wykonaliśmy
        
        do {
            
            SAT = 0;
            
            for (Object e: listaExp){                               //jeżeli podfunkcja jest TRUE to zwiększamy SAT
            Expression E = (Expression)e;
            if (((Boolean)E.evaluate(jexlContext)) == true) SAT++;  
            }
            
            System.out.println ("SAT = " + SAT);
            System.out.println();
            
            if (SAT == listaExp.size() ) break;
            
            for (int j=0; j<listaZM.size(); j++){                                           //iterujemy po zmiennych 
               String temp = "" + listaZM.get(j);                                           //jak zmienna jest true to zmieniamy na false i na odwrót
               System.out.println ("Zmieniamy zmienną " + temp);
               System.out.println();
               if ((Boolean)jexlContext.get(temp) == true)  jexlContext.set(temp, false);
               else jexlContext.set(temp, true);
               
               wypiszZmienne(listaZM, jexlContext);
               
               for (Object e: listaExp){
                    Expression E = (Expression)e;
                    if (((Boolean)E.evaluate(jexlContext)) == true) tSAT++;  
               }
               
               System.out.println ("tSAT = " + tSAT);
               System.out.println();
               
               if (tSAT > SAT){                                      //jeżeli znaleźliśmy przypadek, że tSAT > SAT to wracamy do j=0 ze zmienioną zmienną
                   System.out.println ("Znaleźliśmy tSAT większy od SAT");
                   System.out.println();
                   SAT = tSAT;
                   tSAT = 0;
                   break;
               } else {                                               //w przeciwnym wypadku odmieniamy zmienną i idziemy do kolejnej
                   if ((Boolean)jexlContext.get(temp) == true)  jexlContext.set(temp, false);
                   else jexlContext.set(temp, true);
                   tSAT = 0;      
                   System.out.println ("Odmieniamy zmienną " + temp);
                   System.out.println();
                   wypiszZmienne(listaZM, jexlContext);
               }
               
               if (j==listaZM.size()-1){                               //jeżeli spróbowaliśmy zmienić wszyskie zmienne
                   if (RestartFlag == HowManyRestarts){                //w przypadku gdy wykonaliśmy wystarczająco wiele randomizacji    
                   System.out.println ("Nie znaleziono spełnienia po wykonaniu " + (HowManyRestarts+1) + " randomizacji");                  
                   System.exit(0);
                   }
                   else {
                       
                       RestartFlag++;                                   //jak nie to znowu randomizujemy i zwiększamy flagę
                       for (Object l : listaZM) {
                       String tempx = "" + l;
                       boolean rndB = r[rand.nextInt(2)];
                       jexlContext.set(tempx, rndB);           
                       }
                       
                       
                       System.out.println();
                       System.out.println();
                       System.out.println ("Restart nr" + RestartFlag);
                       System.out.println();
                       
                       wypiszZmienne(listaZM, jexlContext);
                       
                       break;
                       
                   }
               }
               
            }
            
            
        } while (SAT != listaExp.size());
        
        System.out.println();
        System.out.println();
        System.out.println();
        
        System.out.print("Formuła " + exp + " spełniona dla ");
        System.out.print("wartości: [");
                    for (Object l : listaZM) {
                        String tempx = "" + l;
                        boolean test = (Boolean)jexlContext.get(tempx);
                        System.out.print(tempx + ": " + test + " "); 
                     }
                System.out.println("]");
                System.out.println();
        if (RestartFlag > 0) System.out.println("Rozwiązanie osiągnięte po " + (RestartFlag+1) + " randomizacjach");  
        else System.out.println("Rozwiązanie osiągnięte po " + (RestartFlag+1) + " randomizacji");
        
        System.out.println();
        System.out.println();
        
    } 
        
        
    else {                                                  //wersja bez wypisywania
        
            
        Random rand = new Random();
        boolean [] r = {true, false};
        
        for (Object l : listaZM) {                          //dajemy losowe wartości dla wszystkich zmiennych
            String temp = "" + l;
            boolean rndB = r[rand.nextInt(2)];
            jexlContext.set(temp, rndB);                    
        }
        
        int SAT;                                        //SAT - ile podformuł jest spełnionych
        int tSAT = 0;                                       //tSAT - tu przechowujemy informacje o tym czy po zmianie zmiennej zwiększył się SAT
        int RestartFlag = 0;                                //kontrolujemy ile randomizacji wykonaliśmy
        
        do {
            
            SAT = 0;
            
            for (Object e: listaExp){                               //jeżeli podfunkcja jest TRUE to zwiększamy SAT
            Expression E = (Expression)e;
            if (((Boolean)E.evaluate(jexlContext)) == true) SAT++;  
            }
            
            if (SAT == listaExp.size() ) break;
            
            for (int j=0; j<listaZM.size(); j++){                                           //iterujemy po zmiennych 
               String temp = "" + listaZM.get(j);                                           //jak zmienna jest true to zmieniamy na false i na odwrót
               if ((Boolean)jexlContext.get(temp) == true)  jexlContext.set(temp, false);
               else jexlContext.set(temp, true);       
               
               for (Object e: listaExp){
                    Expression E = (Expression)e;
                    if (((Boolean)E.evaluate(jexlContext)) == true) tSAT++;  
               }              
               
               if (tSAT > SAT){                                      //jeżeli znaleźliśmy przypadek, że tSAT > SAT to wracamy do j=0 ze zmienioną zmienną
                   SAT = tSAT;
                   tSAT = 0;
                   break;
               } else {                                               //w przeciwnym wypadku odmieniamy zmienną i idziemy do kolejnej
                   if ((Boolean)jexlContext.get(temp) == true)  jexlContext.set(temp, false);
                   else jexlContext.set(temp, true);
                   tSAT = 0;         
               }
               
               if (j==listaZM.size()-1){                               //jeżeli spróbowaliśmy zmienić wszyskie zmienne
                   if (RestartFlag == HowManyRestarts){                //w przypadku gdy wykonaliśmy wystarczająco wiele randomizacji    
                   System.out.println ("Nie znaleziono spełnienia po wykonaniu " + (HowManyRestarts+1) + " randomizacji");                  
                   System.exit(0);
                   }
                   else {
                       
                       RestartFlag++;                                   //jak nie to znowu randomizujemy i zwiększamy flagę
                       for (Object l : listaZM) {
                       String tempx = "" + l;
                       boolean rndB = r[rand.nextInt(2)];
                       jexlContext.set(tempx, rndB);           
                       }
                       
                       break;
                       
                   }
               }
               
            }
            
            
        } while (SAT != listaExp.size());
        
        
        System.out.print("Formuła " + exp + " spełniona dla ");
        System.out.print("wartości: [");
                    for (Object l : listaZM) {
                        String tempx = "" + l;
                        boolean test = (Boolean)jexlContext.get(tempx);
                        System.out.print(tempx + ": " + test + " "); 
                     }
                System.out.println("]");
                System.out.println();
        if (RestartFlag > 0) System.out.println("Rozwiązanie osiągnięte po " + (RestartFlag+1) + " randomizacjach");  
        else System.out.println("Rozwiązanie osiągnięte po " + (RestartFlag+1) + " randomizacji");
        
        System.out.println();
        System.out.println();    

    }
    }
    
    public static void wypiszZmienne(ArrayList listaZM, JexlContext jexlContext){
        
        System.out.print("Wartości: [");
        for (Object l : listaZM) {
            String temp = "" + l;
            boolean test = (Boolean)jexlContext.get(temp);
            System.out.print(temp + ": " + test + " "); 
        }
        System.out.println("]");
        System.out.println();
        
    }
    
}
