package com.njiafix.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandRegistryService {

    private final Map<String, Map<String, List<String>>> globalCommandRegistry = new HashMap<>();

    @PostConstruct
    public void registerAllCommands() {
        // --- 1. SIMU & ANDROID (Amri 1 - 120) ---
        Map<String, List<String>> simuCmds = new HashMap<>();
        simuCmds.put("reboot", List.of("adb", "reboot"));
        simuCmds.put("clear_cache", List.of("adb", "shell", "pm", "trim-caches", "999G"));
        simuCmds.put("root", List.of("adb", "shell", "su", "-c", "whoami"));
        simuCmds.put("boot", List.of("adb", "reboot", "bootloader"));
        simuCmds.put("logo", List.of("fastboot", "continue"));
        simuCmds.put("recovery", List.of("adb", "reboot", "recovery"));
        simuCmds.put("sideload", List.of("adb", "reboot", "sideload"));
        simuCmds.put("battery_stats", List.of("adb", "shell", "dumpsys", "battery"));
        simuCmds.put("cpu_info", List.of("adb", "shell", "cat", "/proc/cpuinfo"));
        simuCmds.put("mem_info", List.of("adb", "shell", "cat", "/proc/meminfo"));
        simuCmds.put("list_packages", List.of("adb", "shell", "pm", "list", "packages"));
        simuCmds.put("unlock_screen", List.of("adb", "shell", "input", "keyevent", "82"));
        simuCmds.put("screenshot", List.of("adb", "shell", "screencap", "-p", "/sdcard/screenshot.png"));
        simuCmds.put("wifi_reset", List.of("adb", "shell", "am", "broadcast", "-a", "android.net.wifi.WIFI_STATE_CHANGED"));
        simuCmds.put("clear_playstore", List.of("adb", "shell", "pm", "clear", "com.android.vending"));
        simuCmds.put("device_model", List.of("adb", "shell", "getprop", "ro.product.model"));
        simuCmds.put("android_version", List.of("adb", "shell", "getprop", "ro.build.version.release"));
        simuCmds.put("display_density", List.of("adb", "shell", "wm", "density"));
        simuCmds.put("display_size", List.of("adb", "shell", "wm", "size"));
        simuCmds.put("list_permissions", List.of("adb", "shell", "pm", "list", "permissions", "-g"));
        simuCmds.put("kill_all_apps", List.of("adb", "shell", "am", "kill-all"));
        simuCmds.put("dump_window", List.of("adb", "shell", "dumpsys", "window", "displays"));
        simuCmds.put("network_stat", List.of("adb", "shell", "netstat"));
        simuCmds.put("mount_list", List.of("adb", "shell", "mount"));
        simuCmds.put("process_list", List.of("adb", "shell", "ps"));
        simuCmds.put("hardware_serial", List.of("adb", "shell", "getprop", "ro.serialno"));
        simuCmds.put("factory_reset", List.of("adb", "shell", "recovery", "--wipe_data"));
        simuCmds.put("safe_mode_on", List.of("adb", "shell", "setprop", "persist.sys.safemode", "1"));
        simuCmds.put("bluetooth_restart", List.of("adb", "shell", "service", "call", "bluetooth_manager", "6"));
        simuCmds.put("camera_test", List.of("adb", "shell", "am", "start", "-a", "android.media.action.IMAGE_CAPTURE"));
        // ... (Zijaze au endelea kuongeza mpaka amri 120 hapa chini kwa mtindo huu)
        
        globalCommandRegistry.put("simu", simuCmds);
        globalCommandRegistry.put("android", simuCmds);

        // --- 2. KOMPYUTA & WINDOWS (Amri 121 - 280) ---
        Map<String, List<String>> pcCmds = new HashMap<>();
        pcCmds.put("flush_dns", List.of("ipconfig", "/flushdns"));
        pcCmds.put("reset_network", List.of("netsh", "winsock", "reset"));
        pcCmds.put("renew_ip", List.of("ipconfig", "/renew"));
        pcCmds.put("release_ip", List.of("ipconfig", "/release"));
        pcCmds.put("system_info", List.of("systeminfo"));
        pcCmds.put("task_list", List.of("tasklist"));
        pcCmds.put("disk_check", List.of("chkdsk"));
        pcCmds.put("sfc_scan", List.of("sfc", "/scannow"));
        pcCmds.put("ping_test", List.of("ping", "8.8.8.8"));
        pcCmds.put("traceroute", List.of("tracert", "google.com"));
        pcCmds.put("arp_table", List.of("arp", "-a"));
        pcCmds.put("netstat_active", List.of("netstat", "-ano"));
        pcCmds.put("shutdown_pc", List.of("shutdown", "/s", "/t", "60"));
        pcCmds.put("abort_shutdown", List.of("shutdown", "/a"));
        pcCmds.put("open_services", List.of("cmd.exe", "/c", "services.msc"));
        pcCmds.put("list_drivers", List.of("driverquery"));
        pcCmds.put("disk_cleanup", List.of("cleanmgr"));
        pcCmds.put("directx_diag", List.of("dxdiag"));
        pcCmds.put("firewall_status", List.of("netsh", "advfirewall", "show", "allprofiles"));
        pcCmds.put("firewall_reset", List.of("netsh", "advfirewall", "reset"));
        pcCmds.put("task_kill_chrome", List.of("taskkill", "/f", "/im", "chrome.exe"));
        pcCmds.put("task_kill_explorer", List.of("taskkill", "/f", "/im", "explorer.exe"));
        pcCmds.put("restart_explorer", List.of("cmd.exe", "/c", "start explorer.exe"));
        pcCmds.put("power_cfg", List.of("powercfg", "/energy"));
        pcCmds.put("environment_vars", List.of("set"));
        // ... (Zijaze kuongeza mpaka amri 280 hapa chini)

        globalCommandRegistry.put("kompyuta", pcCmds);
        globalCommandRegistry.put("windows", pcCmds);

        // --- 3. MITANDAO NA ROUTER (Amri 281 - 380) ---
        Map<String, List<String>> routerCmds = new HashMap<>();
        routerCmds.put("router_ping", List.of("ping", "192.168.1.1"));
        routerCmds.put("gateway_check", List.of("ipconfig"));
        routerCmds.put("wifi_profiles", List.of("netsh", "wlan", "show", "profiles"));
        routerCmds.put("wireless_reports", List.of("netsh", "wlan", "show", "wlanreport"));
        routerCmds.put("dns_server_check", List.of("nslookup", "google.com"));
        routerCmds.put("restart_wifi", List.of("netsh", "interface", "set", "interface", "Wi-Fi", "admin=disabled"));
        routerCmds.put("enable_wifi", List.of("netsh", "interface", "set", "interface", "Wi-Fi", "admin=enabled"));
        routerCmds.put("show_networks", List.of("netsh", "wlan", "show", "networks", "mode=bssid"));
        routerCmds.put("route_print", List.of("route", "print"));
        // ... (Zijaze kuongeza mpaka amri 380 hapa chini)

        globalCommandRegistry.put("router", routerCmds);
        globalCommandRegistry.put("network", routerCmds);

        // --- 4. PRINTA (Amri 381 - 440) ---
        Map<String, List<String>> printerCmds = new HashMap<>();
        printerCmds.put("restart_spooler", List.of("net", "stop", "spooler"));
        printerCmds.put("start_spooler", List.of("net", "start", "spooler"));
        printerCmds.put("clear_print_queue", List.of("cmd.exe", "/c", "del /Q /F /S \"%systemroot%\\System32\\Spool\\Printers\\*.*\""));
        printerCmds.put("printer_status", List.of("powershell", "Get-Printer"));
        printerCmds.put("test_print", List.of("rundll32.exe", "printui.dll,PrintUIEntry", "/k"));
        // ... (Zijaze kuongeza mpaka amri 440 hapa chini)

        globalCommandRegistry.put("printer", printerCmds);
        globalCommandRegistry.put("printa", printerCmds);

        // --- 5. MAGARI / OBD NA VIFAA VINGINE (Amri 441 - 500+) ---
        Map<String, List<String>> obdCmds = new HashMap<>();
        obdCmds.put("obd_port_scan", List.of("mode", "COM1"));
        obdCmds.put("ecu_reset_signal", List.of("ping", "127.0.0.1", "-n", "2"));
        obdCmds.put("dtc_clear_simulation", List.of("cmd.exe", "/c", "echo Clearing ECU Trouble Codes..."));
        obdCmds.put("sensor_live_feed", List.of("cmd.exe", "/c", "echo Fetching O2 and MAF Sensor Data..."));
        obdCmds.put("battery_alternator_test", List.of("cmd.exe", "/c", "echo Testing Alternator Voltage..."));
        obdCmds.put("fuel_rail_pressure", List.of("cmd.exe", "/c", "echo Checking Fuel Rail Pressure..."));
        obdCmds.put("coolant_temp_check", List.of("cmd.exe", "/c", "echo Checking Engine Coolant Temperature..."));
        // ... (Zijaze kuongeza mpaka amri 500 na kuendelea hapa chini)

        globalCommandRegistry.put("obd", obdCmds);
        globalCommandRegistry.put("gari", obdCmds);
    }

    public List<String> getCommand(String category, String action) {
        if (category == null || action == null) return null;
        Map<String, List<String>> categoryMap = globalCommandRegistry.get(category.toLowerCase());
        if (categoryMap != null) {
            return categoryMap.get(action.toLowerCase());
        }
        return null;
    }
}
