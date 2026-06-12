package com.yani.v380animalguardpro38.siren;

import com.yani.v380animalguardpro38.model.AppConfig;
import java.io.InputStream; import java.io.OutputStream; import java.net.InetSocketAddress; import java.net.Socket; import java.text.SimpleDateFormat; import java.util.Date; import java.util.Locale;

public class SirenController {
    private static final String NEW_RELAY = "172.104.202.253";
    private static final String OLD_RELAY = "172.104.151.204";
    private static final String HELLO_2D = "2d010000ea030000";
    private static final String HELLO_79 = "79010000ea030000";
    private static final String OPEN_2F = "2f01000001300000" + zeros(248);
    private static final String SESSION_0121_A = "01210000000000000110000000000000";
    private static final String SESSION_0121_B = "01210000000000000010000000000000";
    private static final String KEEP_02 = "b9000000e90300000200000000000000";
    private static final String D_EA03_1E = "b9000000ea0300001e00000000000000";
    private static final String E_E903_3C = "b9000000e90300003c00000000000000";
    private static final String BC_ZERO = "bc000000000000000000000000000000";
    private static final String ACK_3C_17 = "8d1300003c00ea2c0000000000000000";
    private static final String ACK_3C_13 = "8d1300003c00401d0000000000000000";
    private static final String ACK_5A = "8d1300005a007e2c0000000000000000";
    private static final String ACK_78 = "8d130000780032000000000000000000";
    private static final String ACK_96 = "8d130000960032000000000000000000";

    public interface Callback { void done(String result); }
    public void runAsync(AppConfig cfg, String mode, Callback cb) { new Thread(() -> { String r = run(cfg, mode); if (cb != null) cb.done(r); }).start(); }

    public String run(AppConfig cfg, String mode) {
        String host = cfg.relayHost;
        if (mode.startsWith("OLD_")) { host = OLD_RELAY; mode = mode.substring(4); }
        if (mode.startsWith("NEW_")) { host = NEW_RELAY; mode = mode.substring(4); }
        String tunnel = mode.startsWith("T79_") ? HELLO_79 : HELLO_2D;
        if (mode.startsWith("T79_")) mode = mode.substring(4);
        return run(host, cfg.relayPort, cfg.deviceHost, mode, tunnel);
    }

    private String run(String host, int port, String deviceHost, String mode, String helloPrefix) {
        Socket s = null; StringBuilder log = new StringBuilder();
        try {
            s = new Socket(); s.connect(new InetSocketAddress(host, port), 6000); s.setSoTimeout(1300);
            InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream();
            log.append(ts()).append(" connected ").append(host).append(":").append(port).append(" mode=").append(mode).append("\n");
            send(out,in,buildHello(deviceHost, helloPrefix), helloPrefix.equals(HELLO_79)?"HELLO 79":"HELLO 2D",log,1200);
            if (mode.equals("HELLO_ONLY")) return finish(log,in,1500);
            send(out,in,hex(OPEN_2F),"OPEN 2F",log,700);
            send(out,in,hex(SESSION_0121_A),"SESSION 0121 A",log,800);
            if (mode.equals("HANDSHAKE")) return finish(log,in,2000);
            if (mode.equals("SESSION_B")) { send(out,in,hex(SESSION_0121_B),"SESSION 0121 B",log,1200); return finish(log,in,1500); }
            warm(out,in,log, mode.equals("KEEP_LONG") ? 10000 : 2500);
            switch(mode) {
                case "D_ONCE": send(out,in,hex(D_EA03_1E),"SIREN D ea03/1e",log,1800); break;
                case "D_TWICE": send(out,in,hex(D_EA03_1E),"SIREN D first",log,900); sleep(900); send(out,in,hex(D_EA03_1E),"SIREN D second",log,1800); break;
                case "D_KEEP": send(out,in,hex(D_EA03_1E),"SIREN D then keep",log,800); warm(out,in,log,4500); break;
                case "E_3C": send(out,in,hex(E_E903_3C),"CANDIDATE E e903/3c",log,1800); break;
                case "E_TWICE": send(out,in,hex(E_E903_3C),"E first",log,900); sleep(900); send(out,in,hex(E_E903_3C),"E second",log,1800); break;
                case "E_THEN_D": send(out,in,hex(E_E903_3C),"E first",log,900); sleep(700); send(out,in,hex(D_EA03_1E),"D after E",log,1800); break;
                case "D_THEN_E": send(out,in,hex(D_EA03_1E),"D first",log,900); sleep(700); send(out,in,hex(E_E903_3C),"E after D",log,1800); break;
                case "BC_THEN_D": send(out,in,hex(BC_ZERO),"BC zero",log,700); sleep(500); send(out,in,hex(D_EA03_1E),"D after BC",log,1800); break;
                case "BC_THEN_E": send(out,in,hex(BC_ZERO),"BC zero",log,700); sleep(500); send(out,in,hex(E_E903_3C),"E after BC",log,1800); break;
                case "PCAP_1719": send(out,in,hex(ACK_3C_17),"ACK 3C from 17:19",log,500); send(out,in,hex(E_E903_3C),"E 3C",log,700); send(out,in,hex(D_EA03_1E),"D 1E",log,1800); break;
                case "PCAP_1714": send(out,in,hex(ACK_5A),"ACK 5A from 17:14",log,500); send(out,in,hex(D_EA03_1E),"D 1E",log,1800); break;
                case "ACKS_THEN_D": send(out,in,hex(ACK_3C_17),"ACK 3C",log,300); send(out,in,hex(ACK_78),"ACK 78",log,300); send(out,in,hex(ACK_96),"ACK 96",log,300); send(out,in,hex(D_EA03_1E),"D after ACKs",log,1800); break;
                case "ACKS_THEN_E": send(out,in,hex(ACK_3C_13),"ACK old 3C",log,300); send(out,in,hex(ACK_78),"ACK 78",log,300); send(out,in,hex(ACK_96),"ACK 96",log,300); send(out,in,hex(E_E903_3C),"E after ACKs",log,1800); break;
                case "KEEP_LONG": break;
                default: log.append(ts()).append(" unknown mode: ").append(mode).append("\n");
            }
            return finish(log,in,2000);
        } catch (Throwable t) { log.append(ts()).append(" ERROR ").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append("\n"); return log.toString(); }
        finally { try { if (s != null) s.close(); } catch(Throwable ignored){} }
    }
    private static String finish(StringBuilder log, InputStream in, long ms) { readBurst(in,log,ms); log.append(ts()).append(" finished\n"); return log.toString(); }
    private static void warm(OutputStream out, InputStream in, StringBuilder log, long ms) throws Exception { long end=System.currentTimeMillis()+ms; int n=0; while(System.currentTimeMillis()<end){ send(out,in,hex(KEEP_02),"KEEP b900/e903/02",log,80); n++; sleep(400);} log.append(ts()).append(" warm keepalives=").append(n).append("\n"); }
    private static void send(OutputStream out, InputStream in, byte[] data, String label, StringBuilder log, long readMs) throws Exception { out.write(data); out.flush(); log.append(ts()).append(" sent ").append(label).append(" len=").append(data.length).append("\n"); readBurst(in,log,readMs); }
    private static void readBurst(InputStream in, StringBuilder log, long ms) { try { byte[] b=new byte[8192]; int total=0,reads=0; long end=System.currentTimeMillis()+ms; while(System.currentTimeMillis()<end){ int a=in.available(); if(a<=0){sleep(30); continue;} int n=in.read(b,0,Math.min(b.length,a)); if(n>0){total+=n; reads++;}} if(total>0) log.append(ts()).append(" received approx ").append(total).append(" bytes in ").append(reads).append(" read(s)\n"); } catch(Throwable ignored){} }
    private static byte[] buildHello(String deviceHost, String prefixHex){ byte[] p=new byte[256]; byte[] pre=hex(prefixHex); System.arraycopy(pre,0,p,0,pre.length); byte[] h=deviceHost.getBytes(); System.arraycopy(h,0,p,8,Math.min(h.length,50)); putHex(p,64,"ba130000cfc22807b3150000d924cd080000000015011000000000000000010101"); return p; }
    private static void putHex(byte[] target,int offset,String s){ byte[] b=hex(s); System.arraycopy(b,0,target,offset,Math.min(b.length,target.length-offset)); }
    private static byte[] hex(String s){ s=s.replace(" ","").replace("\n","").replace("\r",""); byte[] out=new byte[s.length()/2]; for(int i=0;i<out.length;i++) out[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16); return out; }
    private static String zeros(int bytes){ StringBuilder sb=new StringBuilder(); for(int i=0;i<bytes;i++) sb.append("00"); return sb.toString(); }
    private static void sleep(long ms){ try{Thread.sleep(ms);}catch(Throwable ignored){} }
    private static String ts(){ return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()); }
}
