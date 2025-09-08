package util.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.Proxy.ProxyType;

public class ProxyChain {
    private List<ProxyData> proxies;
    private boolean sequential;
    private int chainLength;

    public ProxyChain(List<ProxyData> proxies) {
        this.proxies = proxies;
        this.sequential = false;
        this.chainLength = proxies.size();
    }

    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }

    public boolean isSequential() {
        return sequential;
    }

    public void setChainLength(int chainLength) {
        this.chainLength = chainLength;
    }

    public int getChailnLength() {
        return chainLength;
    }

    public void connect() {

    }

    private void connectSequentially(InetAddress target, int port) {
        List<Socket> proxyChain = new ArrayList<>();

        for (int i = 0; i < chainLength; i++) {
            ProxyData proxyData = proxies.get(i);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyData.getIp(), proxyData.getPort()));
            
            try (Socket socket = new Socket(proxy)) {
                if (proxyChain.isEmpty()) {
                    proxyChain.add(socket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectRandomly(InetAddress target, int port) {
        // Implementation for random connection through proxies
    }

    public void chainProxies(String target, int port) throws IOException {
        Socket headSocket = new Socket();
        headSocket.setSoTimeout(30000);
        headSocket.connect(new InetSocketAddress(proxies.get(0).getIp(), proxies.get(0).getPort()), 30000);
        
        boolean socks5Support = true;

        try {
            byte responseCode = Socks5.sendManualSocksHandshake(headSocket, target, port);
            Socks5.handleSocksResponse(responseCode);
        } catch (IOException e) {
            socks5Support = false;
        }

        if (!socks5Support) {
            try {
                byte responseCode = Socks4.sendManualSocksHandshake(headSocket, target, port);
                Socks4.handleSocksResponse(responseCode);
            } catch (IOException e) {
                throw new IOException("Failed to connect to target through proxy chain using both Socks5 and Socks4 protocols.");
            }
        }
    }
}
