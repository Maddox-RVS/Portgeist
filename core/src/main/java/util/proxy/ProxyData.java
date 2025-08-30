package util.proxy;

import org.checkerframework.checker.units.qual.t;

public class ProxyData {
    public enum Anonymity {
        TRANSPARENT,
        ANONYMOUS,
        ELITE,
        UNKNOWN
    }

    private String ip;
    private int port;
    private String country;
    private Anonymity anonymity;

    public ProxyData(String ip, int port, String country, Anonymity anonymity) {
        this.ip = ip;
        this.port = port;
        this.country = country;
        this.anonymity = anonymity;
    }

    public ProxyData(String ip, int port, String country) {
        this.ip = ip;
        this.port = port;
        this.country = country;
        this.anonymity = Anonymity.UNKNOWN;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getCountry() {
        return country;
    }

    public Anonymity getAnonymity() {
        return anonymity;
    }

    public void setAnonymity(Anonymity anonymity) {
        this.anonymity = anonymity;
    }

    @Override
    public String toString() {
        return "ProxyData{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", country='" + country + '\'' +
                '}';
    }
}
