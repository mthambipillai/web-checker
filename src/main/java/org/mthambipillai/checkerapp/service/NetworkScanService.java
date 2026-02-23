package org.mthambipillai.checkerapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NetworkScanService {
    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    public boolean isOpen(String ip, int port) {
        System.out.println("Trying to connect to " + ip + ":" + port + "...");
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), (int) TIMEOUT.toMillis());
            OutputStream out = socket.getOutputStream();
            out.write("HEAD / HTTP/1.1\r\nHost: x\r\n\r\n".getBytes());
            out.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getParticipantsFromList(List<String> ipsToScan, int port) {
        List<String> result = new ArrayList<>();
        for (String ip : ipsToScan) {
            if (isOpen(ip, port)) {
                result.add(ip);
            }
        }
        System.out.println("Found " + result.size() + " online IPs.");
        return result;
    }

    public List<String> getParticipantsFromSubnet(String subnet, int port) {
        List<String> ips = getAllIPsFromSubnet(subnet);
        return getParticipantsFromList(ips, port);
    }

    private static List<String> getAllIPsFromSubnet(String cidr) {
        List<String> result = new ArrayList<>();
        try {
            String[] parts = cidr.split("/");
            String baseIp = parts[0];
            int prefix = Integer.parseInt(parts[1]);
            InetAddress inet = InetAddress.getByName(baseIp);
            byte[] bytes = inet.getAddress();
            int mask = 0xffffffff << (32 - prefix);
            int ip =
                    ((bytes[0] & 0xff) << 24) |
                            ((bytes[1] & 0xff) << 16) |
                            ((bytes[2] & 0xff) << 8) |
                            (bytes[3] & 0xff);
            int network = ip & mask;
            int broadcast = network | ~mask;
            for (int i = network + 1; i < broadcast; i++) {
                String address =
                        ((i >> 24) & 0xff) + "." +
                                ((i >> 16) & 0xff) + "." +
                                ((i >> 8) & 0xff) + "." +
                                (i & 0xff);
                result.add(address);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

