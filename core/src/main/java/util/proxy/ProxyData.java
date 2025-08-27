package util.proxy;

public class ProxyData {
    private String ip;
    private int port;
    private String country;

    public ProxyData(String ip, int port, String country) {
        this.ip = ip;
        this.port = port;
        this.country = country;
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

    @Override
    public String toString() {
        return "ProxyData{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", country='" + country + '\'' +
                '}';
    }
}
