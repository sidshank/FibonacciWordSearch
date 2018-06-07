import java.util.Scanner;

import com.klocfun.KlocWorkSearch;
import com.klocfun.FastKlocWorkSearch;

public class KlocWorkDriver {
    public static void main(String... args) {
        Scanner sc = new Scanner(System.in);
        while(true) {
            System.out.print("Enter the value of N (-1 to exit):");
            int n = sc.nextInt();
            if (n == -1) {
                break;
            }
            System.out.print("Enter the search string: ");
            String searchStr = sc.next();
            FastKlocWorkSearch fkws = new FastKlocWorkSearch(n, searchStr);
            System.out.println(fkws.computeOccurrences()); 
        }
    }
}