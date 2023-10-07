package ru.vidtu.ias.auth.ms;

public enum AuthStage {
    // For adding only
    OPEN_BROWSER,
    CODE_TO_MS,

    // For refreshing only
    MSR_TO_MS,

    // For both
    INITIALIZING,
    MSA_TO_XBL,
    XBL_TO_XSTS,
    XSTS_TO_MA,
    MA_TO_PROFILE,
    FINISHING;
}
