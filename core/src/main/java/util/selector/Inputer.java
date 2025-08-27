package util.selector;

import java.util.Scanner;
import util.Colors;
import util.TermInstructs;

public class Inputer {
    public static Boolean askYesOrNo(String question) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(Colors.BRIGHT_BLACK + question + " (y/n): " + Colors.RESET);
                String response = scanner.nextLine().trim().toLowerCase();
    
                if (!response.equals("y") && !response.equals("n")) {
                    System.out.println(Colors.RED + "Invalid input. Please enter 'y' or 'n'." + Colors.RESET);
                    continue;
                }
    
                return response.equals("y");
            }
        }
    }
}