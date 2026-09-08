package org.njiafix;

// MUHIMU: Faili hii inategemea maktaba ya "AdbLib" (open-source, Java,
// inatekeleza itifaki rasmi ya ADB - https://github.com/cgutman/AdbLib).
// Lazima uiongeze kama dependency (angalia buildozer_spec_additions.txt).
//
// Hii ni SKELETON - muundo sahihi wa usanifu - lakini itahitaji
// kurekebishwa na kujaribiwa na developer mwenye ujuzi wa Android
// kabla ya matumizi ya kibiashara. Class/method names za AdbLib
// zinaweza kutofautiana kidogo kulingana na toleo utakalotumia.

import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;

import java.io.File;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class NjiafixAdbBridge {

    private Socket socket;
    private AdbConnection connection;
    private AdbCrypto crypto;

    // Hatua 1: Tengeneza / pakia RSA keypair (inahitajika kila mara ADB
    // inapozungumza na kifaa - ni "utambulisho" wa app yako kwa simu
    // ya mteja, kama vile laptop ya kawaida ina adbkey.pub)
    public boolean setupCrypto(String keyStoragePath) {
        try {
            File pub = new File(keyStoragePath, "njiafix_adbkey.pub");
            File priv = new File(keyStoragePath, "njiafix_adbkey");

            if (pub.exists() && priv.exists()) {
                crypto = AdbCrypto.loadAdbKeyPair(
                        new com.cgutman.adblib.AdbBase64() {
                            @Override
                            public String encodeToString(byte[] data) {
                                return android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
                            }
                        }, priv, pub);
            } else {
                crypto = AdbCrypto.generateAdbKeyPair(
                        new com.cgutman.adblib.AdbBase64() {
                            @Override
                            public String encodeToString(byte[] data) {
                                return android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
                            }
                        });
                crypto.saveAdbKeyPair(priv, pub);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hatua 2: Unganisha na simu ya mteja kwa IP + port
    // (mmiliki wa simu ya mteja lazima awe ameshaonyesha "Wireless
    // debugging" IP/Port kutoka Developer Options, na akubali
    // "pairing" dialog itakayotokea kwenye simu yake)
    public boolean connect(String ipAddress, int port) {
        try {
            socket = new Socket(ipAddress, port);
            connection = AdbConnection.create(socket, crypto);
            connection.connect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hatua 3: Tuma amri ya shell, pata majibu kama String
    // (hii ndiyo inayotumika kupata diagnostics: dumpsys battery,
    // df, pm list packages, n.k.)
    public String runShellCommand(String command) {
        try {
            AdbStream stream = connection.open("shell:" + command);
            StringBuilder output = new StringBuilder();
            while (!stream.isClosed()) {
                byte[] data = stream.read();
                if (data == null) break;
                output.append(new String(data));
            }
            return output.toString();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public void disconnect() {
        try {
            if (connection != null) connection.close();
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }
}
