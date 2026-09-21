package pro.sketchware.utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BinaryExecutor {

    private final ProcessBuilder mProcess = new ProcessBuilder();
    private final StringWriter mWriter = new StringWriter();
    private int exitCode = -1;

    public void setCommands(ArrayList<String> arrayList) {
        mProcess.command(arrayList);
    }

    public void setCommands(List<String> list) {
        mProcess.command(list);
    }

    public String execute() {
        try {
            Process process = mProcess.start();
            Scanner scanner = new Scanner(process.getErrorStream());
            while (scanner.hasNextLine()) {
                mWriter.append(scanner.nextLine());
                mWriter.append(System.lineSeparator());
            }
            exitCode = process.waitFor();
            if (exitCode != 0 && mWriter.toString().isEmpty()) {
                mWriter.append("Process exited with code ").append(String.valueOf(exitCode)).append(" but produced no stderr output (possibly killed/crashed).");
            }
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(mWriter));
        }
        return mWriter.toString();
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getLog() {
        return mWriter.toString();
    }
}