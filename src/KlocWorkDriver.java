import java.util.Scanner;

import com.klocfun.KlocWorkSearch;
import com.klocfun.FasterKlocWorkSearch;

public class KlocWorkDriver {
    public static void main(String... args) {
        int n = Integer.parseInt(args[0]);
        String substr = args[1];
        KlocWorkSearch kws = new KlocWorkSearch(n, substr);
        FasterKlocWorkSearch fkws = new FasterKlocWorkSearch(n, substr);
        System.out.println(kws.findOccurrences());
        System.out.println(fkws.findOccurrences());
    }
}