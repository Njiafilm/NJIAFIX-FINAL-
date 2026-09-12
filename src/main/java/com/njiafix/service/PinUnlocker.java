Import java.io.IOException;

public class PinUnlocker {
    public static void unlockWithPin(String pin) {
        try {
            Runtime rt = Runtime.getRuntime();
            
            // 1. Washa skrini
            rt.exec("adb shell input keyevent 26");
            Thread.sleep(1000);
            
            // 2. Telezesha juu kuleta sehemu ya kuweka PIN
            rt.exec("adb shell input swipe 500 1500 500 500 300");
            Thread.sleep(1000);
            
            // 3. Andika PIN husika
            rt.exec("adb shell input text " + pin);
            Thread.sleep(500);
            
            // 4. Bonyeza Enter kuthibitisha
            rt.exec("adb shell input keyevent 66");
            
            System.out.println("PIN imetumwa kwenye simu!");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
Naiwe
