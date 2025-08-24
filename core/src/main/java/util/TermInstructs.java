package util;

public class TermInstructs {
    public static void CLEAR_SCREEN() {
        System.out.print("\033[H\033[2J");
    }

    public static void MOVE_CURSOR_TO_LINE_BEG() {
        System.out.print("\033[0G");
    }

    public static void MOVE_CURSOR_UP(int lines) {
        System.out.print("\033[" + lines + "A");
    }

    public static void MOVE_CURSOR_DOWN(int lines) {
        System.out.print("\033[" + lines + "B");
    }

    public static void MOVE_CURSOR_RIGHT(int spaces) {
        System.out.print("\033[" + spaces + "C");
    }

    public static void MOVE_CURSOR_LEFT(int spaces) {
        System.out.print("\033[" + spaces + "D");
    }

    public static void ERASE_LINE() {
        System.out.print("\033[2K");
    }
}
