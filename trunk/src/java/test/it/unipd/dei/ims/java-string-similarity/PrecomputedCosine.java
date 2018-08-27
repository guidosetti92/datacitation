import info.debatty.java.stringsimilarity.*;
import java.util.Map;
//import info.debatty.java.stringsimilarity.StringProfile;


/**
 * Example of computing cosine similarity with pre-computed profiles.
 */
public class PrecomputedCosine {

    public static void main(String[] args) throws Exception {
        String s1 = "ABCD";
        String s2 = "";

        // Let's work with sequences of 2 characters...
        Cosine cosine = new Cosine(2);

        // Pre-compute the profile of strings
        Map<String, Integer> profile1 = cosine.getProfile(s1);
        Map<String, Integer> profile2 = cosine.getProfile(s2);


	System.out.println(profile1);

	System.out.println(profile2);

        // Prints 0.516185
        System.out.println(cosine.similarity(profile1, profile2));
    }
}
