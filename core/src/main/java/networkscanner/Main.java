package networkscanner;

import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        NetworkScanner.thoroughPortScan(
            "scanme.nmap.org", 
            5000, 
            NetworkScanner.Protocol.UDP);
    }
}
