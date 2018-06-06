import java.util.Scanner;

import com.klocfun.KlocWorkSearch;
import com.klocfun.FasterKlocWorkSearch;
import com.klocfun.FastKlocWorkSearch;

public class KlocWorkDriver {
    public static void main(String... args) {
        int n = Integer.parseInt(args[0]);
        String substr = args[1];
        FastKlocWorkSearch fkws = new FastKlocWorkSearch(n, substr);
        System.out.println(fkws.findOccurrences());
    }
}