package com.neonac.core.mode;

import com.neonac.api.violation.Violation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public final class ModeLogger {

    private static final Logger logger = Logger.getLogger("NeonAC-Mode");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private PrintWriter writer;
    private File logFile;

    public void init(File dataFolder) {
        logFile = new File(dataFolder, "neonac-mode.log");
        try {
            writer = new PrintWriter(new FileWriter(logFile, true), true);
        } catch (IOException e) {
            logger.warning("[NeonAC] Could not open mode log file");
        }
    }

    public void log(Violation v, String mode) {
        if (writer == null) return;
        String line = LocalDateTime.now().format(FMT) + " [" + mode + "] "
                + v.getPlayerName() + " | " + v.getCheck().getId()
                + " | VL=" + String.format("%.1f", v.getViolationLevel())
                + " | conf=" + String.format("%.2f", v.getConfidence());
        writer.println(line);
    }

    public void shutdown() {
        if (writer != null) writer.close();
    }
}
