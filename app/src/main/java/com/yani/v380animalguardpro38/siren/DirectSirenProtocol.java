package com.yani.v380animalguardpro38.siren;

import com.yani.v380animalguardpro38.model.AppConfig;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectSirenProtocol {
    public interface Callback { void done(boolean ok, String result); }

    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final String HOST = "170.187.189.98";
    private static final int RELAY_PORT = 8800;
    private static final int LOGIN_PORT = 8089;

    private static final String LOGIN_8089 = "000300fe520100007b226964223a32353335373831342c226d6574686f64223a226c6f67696e222c22706172616d73223a7b2276657273696f6e223a33312c2270686f6e6554797065223a313031322c226465766963654964223a3132303131313832332c22646f6d61696e223a223132303131313832332e6e766476722e6e6574222c22706f7274223a383830302c226163636f756e744964223a31312c22757365726e616d65223a2252373433353637353543222c2270617373776f7264223a22544a71464845464b725845357270696847416d716d65704c6a7a6f4c48394b624932433949484e5a6467343d222c2272616e646f6d4b6579223a2243505667683833394d5a46383530696e222c22636f6e6e65637454797065223a302c2273656375726974794c6576656c223a312c2261676f7261223a302c2265637478223a313738313238363436322c22703270496478223a307d7d";
    private static final String H2D = "2d010000ea0300003132303131313832332e6e766476722e6e657400000000000000000000000000000000000000000000000000000000000000ba130000cfc22807b31500002c25cd0800000000150010000000000000000101013e462c6a0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    private static final String H79 = "79010000ea0300003132303131313832332e6e766476722e6e657400000000000000000000000000000000000000000000000000000000000000ba130000cfc22807b31500002c25cd080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    private static final String H2F = "2f010000013000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    private static final String H0121 = "01210000000000000010000000000000";
    private static final String HCRED7F = "7f000000f40300003132303131313832332e6e766476722e6e6574000000000000000000000000000000000000000000000000000000000000006022000052373433353637353543000000000000000000000000000000000000000000007f6f5f64316239455631383768323431344437661e8acf9089de420ab40e6ce8cfc22807bffd36eb3d0437b0f19acf74f71bb62f48e3e7aa00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    private static final String KEEPALIVE_8D = "8d130000a4012b030000000000000000";
    private static final String B900_3C = "b9000000e90300003c00000000000000";
    private static final String B900_02 = "b9000000e90300000200000000000000";
    private static final String BC_SECONDARY = "bc000000000000000000000000000000";

    public static void triggerAsync(AppConfig cfg, String reason, Callback cb) {
        EXEC.execute(() -> {
            String result = trigger(cfg, reason);
            boolean ok = !result.contains(" ERROR ");
            if (cb != null) cb.done(ok, result);
        });
    }

    public static String trigger(AppConfig cfg, String reason) {
        long start = System.currentTimeMillis();
        StringBuilder log = new StringBuilder();
        log.append(ts()).append(" S01 direct siren start: ").append(reason).append("\n");
        Socket control = null;
        Socket secondary = null;
        try {
            String login = sendOneShot(HOST, LOGIN_PORT, LOGIN_8089, 3500, true);
            log.append(ts()).append(" 8089 ").append(login).append("\n");
            sleep(250);

            control = open(HOST, RELAY_PORT, 3500);
            secondary = open(HOST, RELAY_PORT, 3500);
            control.setSoTimeout(120);
            secondary.setSoTimeout(120);

            send(control, H2D); log.append(ts()).append(" sent 2D\n");
            sleep(30);
            send(secondary, H79); log.append(ts()).append(" sent 79\n");
            sleep(80);
            readSome(control, "ctrl91", log);
            readSome(secondary, "secDD", log);

            send(control, H2F); log.append(ts()).append(" sent 2F\n");
            sleep(60);
            send(control, H0121); log.append(ts()).append(" sent 0121\n");
            sleep(400);

            Socket cred = open(HOST, RELAY_PORT, 2500);
            cred.setSoTimeout(120);
            send(cred, HCRED7F); log.append(ts()).append(" sent 7F\n");
            readSome(cred, "cred", log);
            try { cred.close(); } catch (Throwable ignored) {}
            sleep(700);

            send(control, KEEPALIVE_8D); log.append(ts()).append(" sent 8D\n");
            sleep(80);
            send(control, B900_3C); log.append(ts()).append(" sent B900 3C\n");
            sleep(120);
            for (int i = 0; i < 6; i++) {
                send(control, B900_02);
                log.append(ts()).append(" sent B900 02 #").append(i + 1).append("\n");
                sleep(350);
                readSome(control, "after" + i, log);
            }

            send(secondary, BC_SECONDARY); log.append(ts()).append(" sent BC\n");
            sleep(100);
            readSome(secondary, "secAfter", log);
            log.append(ts()).append(" finished in ").append(System.currentTimeMillis() - start).append("ms\n");
            return log.toString();
        } catch (Throwable t) {
            log.append(ts()).append(" ERROR ").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append("\n");
            return log.toString();
        } finally {
            try { if (control != null) control.close(); } catch (Throwable ignored) {}
            try { if (secondary != null) secondary.close(); } catch (Throwable ignored) {}
        }
    }

    private static Socket open(String host, int port, int timeoutMs) throws Exception {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeoutMs);
        socket.setSoTimeout(timeoutMs);
        return socket;
    }

    private static void send(Socket socket, String hex) throws Exception {
        OutputStream out = socket.getOutputStream();
        out.write(fromHex(hex));
        out.flush();
    }

    private static String sendOneShot(String host, int port, String hex, int timeoutMs, boolean read) throws Exception {
        Socket socket = open(host, port, timeoutMs);
        send(socket, hex);
        String response = "sent=" + (hex.length() / 2);
        if (read) {
            byte[] buf = new byte[1024];
            int n = -1;
            try { n = socket.getInputStream().read(buf); } catch (Throwable ignored) {}
            if (n > 0) response += ",respBytes=" + n; else response += ",noResp";
        }
        try { socket.close(); } catch (Throwable ignored) {}
        return response;
    }

    private static void readSome(Socket socket, String label, StringBuilder log) {
        try {
            byte[] buf = new byte[256];
            int n = socket.getInputStream().read(buf);
            if (n > 0) log.append(ts()).append(" ").append(label).append(" respBytes=").append(n).append("\n");
        } catch (Throwable ignored) {}
    }

    private static byte[] fromHex(String hex) {
        String clean = hex == null ? "" : hex.replaceAll("[^0-9A-Fa-f]", "");
        if ((clean.length() % 2) != 0) throw new IllegalArgumentException("Odd hex length");
        byte[] out = new byte[clean.length() / 2];
        for (int i = 0; i < clean.length(); i += 2) out[i / 2] = (byte) Integer.parseInt(clean.substring(i, i + 2), 16);
        return out;
    }

    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (Throwable ignored) {} }
    private static String ts() { return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()); }
}
