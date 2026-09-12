import java.io.IOException;

public class ScreenUnlocker {
    public static void unlockScreen() {
        try {
            Runtime rt = Runtime.getRuntime();
            
            // 1. Washa skrini (Power button / Wake up)
            rt.exec("adb shell input keyevent 26");
            
            // 2. Subiri kidogo sekunde 1 kuupa mfumo muda wa kuamka
            Thread.sleep(1000);
            
            // 3. Telezesha skrini (Swipe kutoka chini kwenda juu - mfano wa kuratibu swipe)
            // Vigezo: adb shell input swipe <x1> <y1> <x2> <y2> <muda_ms>
            rt.exec("adb shell input swipe 500 1500 500 500 300");
            
            System.out.println("Skrini imefunguliwa kikamilifu!");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
