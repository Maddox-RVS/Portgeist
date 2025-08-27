package util;

public class OsUtils {
    public enum OperatingSystem {
        WINDOWS,
        LINUX,
        MACOS,
        UNKNOWN
    }

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    public static OperatingSystem getOS() {
        String os = getOsName().toLowerCase();
        
        if (os.contains("win")) return OperatingSystem.WINDOWS;
        else if (os.contains("mac")) return OperatingSystem.MACOS;
        else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return OperatingSystem.LINUX;
        else return OperatingSystem.UNKNOWN;
    }

    public static String getOsArchitecture() {
        return System.getProperty("os.arch");
    }
}
