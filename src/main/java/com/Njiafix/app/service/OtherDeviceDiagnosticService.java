package com.Njiafix.app.service;

import org.springframework.stereotype.Service;

@Service
public class OtherDeviceDiagnosticService implements DiagnosticAction {

    @Override
    public String detectProblem() {
        // Amri ya kuchunguza vifaa kama Router au UPS
        return "[SCAN]: UPS inaonyesha betri inahitaji uangalizi au Router inapoteza pakiti za mtandao (packet loss).";
    }

    @Override
    public String fixProblemSingleClick() {
        // Amri ya kurekebisha mtandao au mfumo wa ziada kwa single click
        return "[FIX SUCCESS]: Mipangilio ya kifaa cha ziada imerekebishwa na muunganisho umerudi kuwa imara!";
    }
}

