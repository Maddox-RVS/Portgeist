package util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.openqa.selenium.devtools.v121.io.IO;

import util.OsUtils.OperatingSystem;
import util.loading.Spinner;

public class Tor {
    public static boolean isEnabled = false;

    private boolean isTorSupported() {
        OperatingSystem os = OsUtils.getOS();
        return os != OperatingSystem.UNKNOWN;
    }
    
    private String getTorArchive() {
        OperatingSystem os = OsUtils.getOS();
        String arch = OsUtils.getOsArchitecture().toLowerCase();

        switch (os) {
            case WINDOWS: 
                return "tor-binaries/windows/tor-expert-bundle-windows-x86_64-14.5.6.tar.gz";
            case MACOS:
                if (arch.contains("aarch64") || arch.contains("arm"))
                    return "tor-binaries/macos/tor-expert-bundle-macos-aarch64-14.5.6.tar.gz";
                else return "tor-binaries/macos/tor-expert-bundle-macos-x86_64-14.5.6.tar.gz";
            case LINUX: 
                return "tor-binaries/linux/tor-expert-bundle-linux-x86_64-14.5.6.tar.gz";
            case UNKNOWN:
            default:
                return null;
        }
    }

    private Path extractTorExecutable() 
        throws IllegalArgumentException, 
            UnsupportedOperationException, 
            IOException, 
            SecurityException {

            Path torExecutablePath = null;

            Path tempDir = Files.createTempDirectory("tor-runtime-");
            tempDir.toFile().deleteOnExit();

            String torArchivePath = getTorArchive();

            try (
                InputStream archiveResourceStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(torArchivePath); // Forces gradle to rebuild the resource table so that archiveResourceStream isnt unpredictably null, this was annoying to debug
                BufferedInputStream bufferedInput = new BufferedInputStream(archiveResourceStream);
                InputStream gzipInput = new GZIPInputStream(bufferedInput);
                TarArchiveInputStream tarInput = new TarArchiveInputStream(gzipInput);
            ) {
                TarArchiveEntry entry;

                while ((entry = tarInput.getNextEntry()) != null) {
                    Path entryPath = tempDir.resolve(entry.getName());

                    if (entry.isDirectory())
                        Files.createDirectories(entryPath);
                    else {
                        Files.createDirectories(entryPath.getParent());
                        try (OutputStream out = Files.newOutputStream(entryPath)) {
                            tarInput.transferTo(out);
                        }

                        String fileName = Path.of(entry.getName()).getFileName().toString().toLowerCase();
                        if (fileName.equals("tor") || fileName.equals("tor.exe")) {
                            torExecutablePath = entryPath;
                        }
                    }
                }
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.walk(tempDir)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {}
                        });
                } catch (IOException ignored) {}
            }));

            return torExecutablePath;
    }

    public Process startTorProcess() {
        boolean isTorSupported = isTorSupported();
        if (!isTorSupported) {
            System.out.println(Colors.BG_RED + "ERROR" + Colors.RESET + Colors.RED + " Your OS is not supported for Tor execution." + Colors.RESET);
            return null;
        }

        Spinner spinner = new Spinner("Extracting Tor executable");
        spinner.start();

        try {
            Path torExecutablePath = extractTorExecutable();
            spinner.stop();

            if (torExecutablePath == null) {
                System.out.println(Colors.BG_RED + "ERROR" + Colors.RESET + Colors.RED + " Failed to extract Tor executable." + Colors.RESET);
                return null;
            }

            spinner = new Spinner("Starting Tor process");
            spinner.start();

            ProcessBuilder pb = new ProcessBuilder(torExecutablePath.toAbsolutePath().toString());
            Process torProcess = pb.start();

            spinner.stop();

            isEnabled = true;
            return torProcess;

        } catch (IllegalArgumentException | 
            UnsupportedOperationException |
            IOException | 
            SecurityException e) {
                
            spinner.stop();
            System.out.println(Colors.BG_RED + "ERROR" + Colors.RESET + Colors.RED + " Issue extracting and starting Tor executable." + Colors.RESET);
            return null;
        }
    }
}
