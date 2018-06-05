import java.util.Scanner;

import com.klocfun.KlocWorkSearch;

public class KlocWorkDriver {
    public static void main(String... args) {
        Scanner sc = new Scanner(System.in);
        do {
            int n = sc.nextInt();
            // String subStr = sc.next();
            KlocWorkSearch kws = new KlocWorkSearch(n, "");
            System.out.println(kws.getSequence(n));
        } while (sc.hasNext());
    }
}