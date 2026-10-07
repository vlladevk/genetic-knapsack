package pl.pwr.tio.mkp.experiment;

import java.io.IOException;
import java.nio.file.Path;

// Calls scripts/plots.py. If Python is missing, the CSV files are still there.
public final class Plots {

    // relative to the working directory
    private static final Path SCRIPT = Path.of("scripts", "plots.py");

    private final String python;

    public Plots(final String python) {
        this.python = python;
    }

    public void draw(final Path results) {
        boolean success;
        try {
            final Process process = new ProcessBuilder(
                this.python, Plots.SCRIPT.toString(), results.toString()
            ).inheritIO().start();
            success = process.waitFor() == 0;
        } catch (final IOException ex) {
            System.err.println("Could not start Python (" + this.python + "): " + ex.getMessage());
            success = false;
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            success = false;
        }
        if (!success) {
            System.err.println("Plots were not drawn, CSV files are in " + results);
        }
    }
}
