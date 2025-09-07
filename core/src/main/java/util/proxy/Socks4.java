package util.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Socks4 {
    private static final String USERID = "Anonymous";

    private static final byte SOCKS_VERSION_4 = 0x04;
    private static final byte ESTABLISH_TCP_CONNECTION = 0x01;
    private static final byte NULL_TERMINATOR = 0x00;

    private static final byte REQUEST_GRANTED = 0x5A;
    private static final byte REQUEST_REJECTED_OR_FAILED = 0x5B;
    private static final byte REQUEST_FAILED_CANNOT_CONNECT_TO_IDENTD = 0x5C;
    private static final byte REQUEST_FAILED_DIFFERENT_USERID = 0x5D;

    public static void handleSocksResponse(byte responseCode) throws IOException {
        switch (responseCode) {
            case REQUEST_GRANTED: break;
            case REQUEST_REJECTED_OR_FAILED: 
                throw new IOException("Socks4 proxy rejected the connection request or request failed.");
            case REQUEST_FAILED_CANNOT_CONNECT_TO_IDENTD: 
                throw new IOException("Socks4 proxy failed because it could not connect to the identd on the client.");
            case REQUEST_FAILED_DIFFERENT_USERID: 
                throw new IOException("Socks4 proxy failed because the client provided a different user ID than in identd.");
        }
    }

    public static byte sendManualSocksHandshake(Socket socks4Socket, String target, int port) throws IOException, UnknownHostException {
        byte[] connectionRequest = constructManualSocksConnectionRequest(target, port, USERID);

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

    public static byte[] constructManualSocksConnectionRequest(String target, int port, String userid) throws UnknownHostException{
        //TODO: Handle domain names and IPv6 addresses
        
        byte portNumHigh = (byte) ((port >> 8) & 0xFF);
        byte portNumLow = (byte) (port & 0xFF);

        byte[] ipDestination = new byte[4];
        String[] ipParts = InetAddress.getByName(target).getHostAddress().split("\\.");
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
