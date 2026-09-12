package com.njiafix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Transactional
    public boolean verifyAndMarkAsPaid(String companyNumber, String phoneNumber, double amount) {
        // 1. Hakikisha namba ya kampuni ni sahihi (mfano: 560500)
        if (!"560500".equals(companyNumber)) {
            System.err.println("Hitilafu: Namba ya kampuni si sahihi -> " + companyNumber);
            return false;
        }

        // 2. Hakikisha namba ya simu ipo (inatumika kama kumbukumbu ya malipo - Reference)
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            System.err.println("Hitilafu: Namba ya simu ya kumbukumbu haipo.");
            return false;
        }

        // 3. Tekeleza mantiki ya kuhifadhi au kusasisha hali ya malipo kuwa PAID kwenye database
        System.out.println("MALIPO YAMEFANIKIWA:");
        System.out.println("- Kampuni: " + companyNumber);
        System.out.println("- Kumbukumbu (Namba ya Simu): " + phoneNumber);
        System.out.println("- Kiasi: TZS " + amount);
        System.out.println("- Hali: PAID (Imelipwa) tarehe " + LocalDateTime.now());

        // 4. Tuma ujumbe mfupi (SMS) kwa mteja kuthibitisha malipo
        sendPaymentSuccessSms(phoneNumber, amount, companyNumber);

        return true;
    }

    // Njia ya kutuma SMS (Unaweza kuunganisha na API ya gateway ya SMS ya hapa nchini)
    private void sendPaymentSuccessSms(String phoneNumber, double amount, String companyNumber) {
        String smsMessage = String.format(
            "Imepokelewa! Malipo ya TZS %.2f yamethibitishwa na kuwa PAID. Kampuni: %s. Asante kwa kutumia NjiaFix.",
            amount, companyNumber
        );

        // Hapa ndipo unaweza kuweka code za kuita API ya mtandao wa simu au SMS gateway
        System.out.println("----------------------------------------");
        System.out.println("INATUMIA SMS KWENDA: " + phoneNumber);
        System.out.println("UJUMBE: " + smsMessage);
        System.out.println("----------------------------------------");
        
        // Mfano: smsGatewayClient.send(phoneNumber, smsMessage);
    }
}
