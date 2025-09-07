package util.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Socks5 {
    private static final byte SOCKS_VERSION_5 = 0x05;
    private static final byte ESTABLISH_TCP_CONNECTION = 0x01;
    private static final byte NUM_METHODS = 0x01;
    private static final byte NO_AUTHENTICATION_METHOD = 0x00;
    private static final byte RESERVED = 0x00;
    private static final byte ADDRESS_TYPE_IPV4 = 0x01;
    private static final byte ADDRESS_TYPE_DOMAIN_NAME = 0x03;
    private static final byte ADDRESS_TYPE_IPV6 = 0x04;

    private static final byte REQUEST_GRANTED = 0x00;
    private static final byte GENERAL_FAILURE = 0x01;
    private static final byte CONNECTION_NOT_ALLOWED_BY_RULESET = 0x02;
    private static final byte NETWORK_UNREACHABLE = 0x03;
    private static final byte HOST_UNREACHABLE = 0x04;
    private static final byte CONNECTION_REFUSED_BY_DESTINATION_HOST = 0x05;
    private static final byte TTL_EXPIRED = 0x06;
    private static final byte COMMAND_NOT_SUPPORTED_OR_PROTOCOL_ERROR = 0x07;
    private static final byte ADDRESS_TYPE_NOT_SUPPORTED = 0x08;

    public static void handleSocksResponse(byte responseCode) throws IOException {
        switch (responseCode) {
            case REQUEST_GRANTED: break;
            case GENERAL_FAILURE: 
                throw new IOException("Socks5 proxy general failure.");
            case CONNECTION_NOT_ALLOWED_BY_RULESET: 
                throw new IOException("Socks5 proxy connection not allowed by ruleset.");
            case NETWORK_UNREACHABLE: 
                throw new IOException("Socks5 proxy network unreachable.");
            case HOST_UNREACHABLE: 
                throw new IOException("Socks5 proxy host unreachable.");
            case CONNECTION_REFUSED_BY_DESTINATION_HOST: 
                throw new IOException("Socks5 proxy connection refused by destination host.");
            case TTL_EXPIRED: 
                throw new IOException("Socks5 proxy TTL expired.");
            case COMMAND_NOT_SUPPORTED_OR_PROTOCOL_ERROR: 
                throw new IOException("Socks5 proxy command not supported or protocol error.");
            case ADDRESS_TYPE_NOT_SUPPORTED: 
                throw new IOException("Socks5 proxy address type not supported.");
        }
    }

    public static byte sendManualSocksHandshake(Socket socks5Socket, String target, int port) throws IOException, UnknownHostException {
        byte[] authenticationRequest = constructManualSocksAuthenticationRequest();
        byte[] connectionRequest = constructManualSocksConnectionRequest(target, port);

        OutputStream out = socks5Socket.getOutputStream();
        out.write(authenticationRequest);
        out.flush();

        InputStream in = socks5Socket.getInputStream();
        byte[] authResponse = new byte[2];
        int bytesRead = 0;
        while (bytesRead < 2) {
            int result = in.read(authResponse, bytesRead, 2 - bytesRead);
            if (result == -1)
                throw new IOException("End of stream reached before reading full socks5 authentication response.");
            bytesRead += result;
        }

        if (authResponse[0] != SOCKS_VERSION_5 && authResponse[1] != NO_AUTHENTICATION_METHOD)
            throw new IOException("Socks5 proxy does not support no-authentication method.");

        out.write(connectionRequest);
        out.flush();

        byte[] connResponse = new byte[10];
        bytesRead = 0;
        while (bytesRead < 10) {
            int result = in.read(connResponse, bytesRead, 10 - bytesRead);
            if (result == -1)
                throw new IOException("End of stream reached before reading full socks5 connection response.");
            bytesRead += result;
        }

        return connResponse[1];
    }

    public static byte[] constructManualSocksAuthenticationRequest() {
        byte[] authenticationRequest = new byte[3];
        authenticationRequest[0] = SOCKS_VERSION_5;
        authenticationRequest[1] = NUM_METHODS;
        authenticationRequest[2] = NO_AUTHENTICATION_METHOD;
        return authenticationRequest;
    }

    public static byte[] constructManualSocksConnectionRequest(String target, int port) throws UnknownHostException {
        //TODO: Handle domain names and IPv6 addresses
        
        byte portNumHigh = (byte) ((port >> 8) & 0xFF);
        byte portNumLow = (byte) (port & 0xFF);

        if (ProxyData.isIpv4(target)) {
            byte[] ipDestination = new byte[4];
            String[] ipParts = target.strip().split("\\.");
            for (int i = 0; i < 4; i++) ipDestination[i] = (byte) Integer.parseInt(ipParts[i]);

            byte[] connectionRequest = new byte[10];
            connectionRequest[0] = SOCKS_VERSION_5;
            connectionRequest[1] = ESTABLISH_TCP_CONNECTION;
            connectionRequest[2] = RESERVED;
            connectionRequest[3] = ADDRESS_TYPE_IPV4;
            System.arraycopy(ipDestination, 0, connectionRequest, 4, 4);
            connectionRequest[8] = portNumHigh;
            connectionRequest[9] = portNumLow;

            return connectionRequest;
        } else if (ProxyData.isIpv6(target)) {
            byte[] ipDestination = new byte[16];
            String[] ipParts = target.strip().split(":");
            for (int i = 0; i < 16; i++) ipDestination[i] = (byte) Integer.parseInt(ipParts[i], 16);

            byte[] connectionRequest = new byte[22];
            connectionRequest[0] = SOCKS_VERSION_5;
            connectionRequest[1] = ESTABLISH_TCP_CONNECTION;
            connectionRequest[2] = RESERVED;
            connectionRequest[3] = ADDRESS_TYPE_IPV6;
            System.arraycopy(ipDestination, 0, connectionRequest, 4, 16);
            connectionRequest[20] = portNumHigh;
            connectionRequest[21] = portNumLow;

            return connectionRequest;
        } else {
            //TODO: Impliment domain name handling
        }
    }
}