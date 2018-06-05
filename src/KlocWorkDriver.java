import java.util.Scanner;

import com.klocfun.KlocWorkSearch;

public class KlocWorkDriver {
    public static void main(String... args) {
        // Scanner sc = new Scanner(System.in);
        // do {
        //     int n = sc.nextInt();
        //     // String subStr = sc.next();
        //     KlocWorkSearch kws = new KlocWorkSearch(n, "");
        //     System.out.println(kws.computeSequence(n));
        //     kws.printStream();
        // } while (sc.hasNext());
        int n = Integer.parseInt(args[0]);
        String substr = args[1];
        KlocWorkSearch kws = new KlocWorkSearch(n, substr);
        // System.out.println(kws.computeSequence(n));
        // kws.printStream();
        System.out.println(kws.findOccurrences());
    }
}