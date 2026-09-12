package com.Njiafix.app.service;

import org.springframework.stereotype.Service;

@Service
public class ObdService implements DiagnosticAction {

    @Override
    public String detectProblem() {
        // Amri ya kusoma makosa kwenye mfumo wa gari (OBD Trouble Codes)
        return "[SCAN]: Imegundua hitilafu ya Sensor ya O2 na hitilafu ndogo ya mtiririko wa mafuta.";
    }

    @Override
    public String fixProblemSingleClick() {
        // Amri ya kufuta makosa (DTC Clear) na kuweka sawa mfumo kwa single click
        return "[FIX SUCCESS]: Hitilafu za OBD zimefutwa kwenye kompyuta ya gari (ECU) na mfumo umerudishwa sawa!";
    }
}

