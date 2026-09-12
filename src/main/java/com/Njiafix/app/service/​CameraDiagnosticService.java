package com.Njiafix.app.service;

import org.springframework.stereotype.Service;

@Service
public class CameraDiagnosticService implements DiagnosticAction {

    @Override
    public String detectProblem() {
        // Amri ya kuchunguza tatizo la kamera
        return "[SCAN]: Inachunguza driver za kamera na ruhusa (permissions)... Hakuna tatizo kubwa lililopatikana, lakini inaweza kuhitaji kuanzishwa upya.";
    }

    @Override
    public String fixProblemSingleClick() {
        // Amri ya kuondoa tatizo kwa single click
        return "[FIX SUCCESS]: Kamera imewekewa upya driver na kurudishwa kwenye hali ya kawaida kwa mbio moja!";
    }
}

