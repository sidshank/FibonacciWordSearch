import java.util.Scanner;
import com.klocfun.KlocWorkSearch;

public class KlocWorkDriver {

    public static void main(String... args) {
        Scanner sc = new Scanner(System.in);
        double timeScalingFactor = Math.pow(10, 6);

        try {
            while(true) {
                System.out.print("Enter the value of N (-1 to exit): ");

                int n = sc.nextInt();
                if (n == -1) {
                    break;
                }

                System.out.print("Enter the search string: ");
                String searchStr = sc.next();

                long tStart = System.nanoTime();

                KlocWorkSearch fkws = new KlocWorkSearch(n, searchStr);
                long occurrences = fkws.computeOccurrences();

                long tEnd = System.nanoTime();

                System.out.format("\n==========================\n");
                System.out.format("Search completed in %f ms\n", (tEnd - tStart) / timeScalingFactor);
                System.out.format("The number of occurrences of %s in kw(%d) is : %s\n",
                    searchStr, n, occurrences);
                System.out.format("==========================\n\n");
            }
        } finally {
            sc.close();
        }
    }
}