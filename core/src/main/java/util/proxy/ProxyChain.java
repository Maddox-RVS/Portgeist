package util.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.Proxy.ProxyType;

public class ProxyChain {
    private final String USERID = "Anonymous";

    private final byte SOCKS_VERSION_4 = 0x04;
    private final byte SOCKS_VERSION_5 = 0x05;
    private final byte ESTABLISH_TCP_CONNECTION = 0x01;
    private final byte ESTABLISH_TCP_PORT_BINDING = 0x02;
    private final byte NULL_TERMINATOR = 0x00;

    private final byte REQUEST_GRANTED_SOCKS4 = 0x5A;
    private final byte REQUEST_REJECTED_OR_FAILED_SOCKS4 = 0x5B;
    private final byte REQUEST_FAILED_CANNOT_CONNECT_TO_IDENTD_SOCKS4 = 0x5C;
    private final byte REQUEST_FAILED_DIFFERENT_USERID_SOCKS4 = 0x5D;

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
        // byte responseCode = sendManualSocks4Handshake(headSocket, target, port);
        // handleSocks4Response(responseCode);
        // System.out.println("Connected to target through proxy chain.");
    }

    private void handleSocks4Response(byte responseCode) throws IOException {
        switch (responseCode) {
            case REQUEST_GRANTED_SOCKS4: break;
            case REQUEST_REJECTED_OR_FAILED_SOCKS4: 
                throw new IOException("Socks4 proxy rejected the connection request or request failed.");
            case REQUEST_FAILED_CANNOT_CONNECT_TO_IDENTD_SOCKS4: 
                throw new IOException("Socks4 proxy failed because it could not connect to the identd on the client.");
            case REQUEST_FAILED_DIFFERENT_USERID_SOCKS4: 
                throw new IOException("Socks4 proxy failed because the client provided a different user ID than in identd.");
        }
    }

    private byte sendManualSocks4Handshake(Socket socks4Socket, String target, int port) throws IOException {
        byte[] connectionRequest = constructManualSocks4ConnectionRequest(target, port, USERID);

        OutputStream out = socks4Socket.getOutputStream();
        out.write(connectionRequest);
        out.flush();

        InputStream in = socks4Socket.getInputStream();
        byte[] response = new byte[8];
        int bytesRead = 0;
        while (bytesRead < 8) {
            int result = in.read(response, bytesRead, 8 - bytesRead);
            if (result == -1)
                throw new IOException("End of stream reached before reading full socks4 handshake response.");
            bytesRead += result;
        }

        return response[1];
    }

    private byte[] constructManualSocks4ConnectionRequest(String target, int port, String userid) {
        byte portNumHigh = (byte) ((port >> 8) & 0xFF);
        byte portNumLow = (byte) (port & 0xFF);

        byte[] ipDestination = new byte[4];
        String[] ipParts = target.split("\\.");
        for (int i = 0; i < 4; i++) ipDestination[i] = (byte) Integer.parseInt(ipParts[i]);

        byte[] userId = userid.getBytes(StandardCharsets.US_ASCII);

        byte[] connectionRequest = new byte[9 + userId.length];
        connectionRequest[0] = SOCKS_VERSION_4;
        connectionRequest[1] = ESTABLISH_TCP_CONNECTION;
        connectionRequest[2] = portNumHigh;
        connectionRequest[3] = portNumLow;
        System.arraycopy(ipDestination, 0, connectionRequest, 4, 4);
        System.arraycopy(userId, 0, connectionRequest, 8, userId.length);
        connectionRequest[connectionRequest.length - 1] = NULL_TERMINATOR;

        return connectionRequest;
    }
}
