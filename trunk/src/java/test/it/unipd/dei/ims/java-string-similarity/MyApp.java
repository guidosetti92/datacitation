import java.lang.StringBuilder;
import info.debatty.java.stringsimilarity.*;


public class MyApp {


    public static void main(String[] args) {
        JaroWinkler jw = new JaroWinkler();
        
        // substitution of s and t
        System.out.println(jw.similarity("My string", "My string"));
        
        // substitution of s and n
        System.out.println(jw.similarity("My string", "My ntrisg"));
    }

}
