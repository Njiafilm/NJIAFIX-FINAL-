import com.sun.management.OperatingSystemMXBean;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;

/**
 * NjiaFix PC Agent - programu ndogo inayowekwa kwenye KOMPYUTA YA MTEJA
 * (si kompyuta ya fundi/server kuu). Inafanya kazi peke yake bila Spring
 * Boot na bila dependencies za nje - JDK pekee inatosha.
 *
 * JINSI YA KUTUMIA (kwenye kompyuta ya mteja):
 *   1. Nakili faili hii (PcAgent.java) kwenye kompyuta hiyo
 *   2. javac PcAgent.java
 *   3. java PcAgent
 *   4. Agent itaanza kusikiliza kwenye port 5556
 *   5. Kwenye dashboard ya fundi (diagnose-other.html), weka IP ya
 *      kompyuta hii kwenye sehemu ya "PC ya Mteja" ili kuichunguza
 *
 * Kompyuta ya mteja na kompyuta ya fundi lazima ziwe kwenye MTANDAO MMOJA
 * (Wi-Fi/LAN moja) ili hii ifanye kazi bila intaneti ya nje.
 */
public class PcAgent {

    public static void main(String[] args) throws Exception {
        int port = 5556;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/diagnose", PcAgent::handleDiagnose);
        server.setExecutor(null);
        server.start();
        System.out.println("NjiaFix PC Agent inaendesha kwenye port " + port + " ...");
        System.out.println("Fundi anaweza sasa kuchunguza kompyuta hii kwa IP yake.");
        System.out.println("Usifunge dirisha hili wakati fundi anachunguza.");
    }

    private static void handleDiagnose(HttpExchange exchange) throws java.io.IOException {
        StringBuilder json = new StringBuilder("{");

        try {
            OperatingSystemMXBean osBean =
                    (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            double cpuLoad = osBean.getCpuLoad(); // 0.0 - 1.0 (JDK 17+)
            long totalMem = osBean.getTotalMemorySize();
            long freeMem = osBean.getFreeMemorySize();
            int ramFreePercent = totalMem > 0 ? (int) ((freeMem * 100) / totalMem) : -1;

            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            File root = new File(isWindows ? "C:\\" : "/");
            long totalDisk = root.getTotalSpace();
            long usableDisk = root.getUsableSpace();
            int diskUsedPercent = totalDisk > 0 ? (int) (100 - (usableDisk * 100 / totalDisk)) : -1;

            json.append("\"cpu_load_percent\":").append((int) (cpuLoad * 100)).append(",");
            json.append("\"ram_free_percent\":").append(ramFreePercent).append(",");
            json.append("\"disk_used_percent\":").append(diskUsedPercent).append(",");
            json.append("\"os_name\":\"").append(System.getProperty("os.name")).append("\"");
        } catch (Exception e) {
            json.append("\"error\":\"").append(e.getMessage()).append("\"");
        }

        json.append("}");

        byte[] response = json.toString().getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
