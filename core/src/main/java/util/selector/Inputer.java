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
                    TermInstructs.MOVE_CURSOR_UP(1);
                    TermInstructs.ERASE_LINE();
                    TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                    continue;
                }
    
                return response.equals("y");
            }
        }
    }
}