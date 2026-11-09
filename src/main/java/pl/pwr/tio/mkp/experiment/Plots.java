package pl.pwr.tio.mkp.experiment;

import java.io.IOException;
import java.nio.file.Path;

// Calls a plotting script from scripts/. If Python is missing, the CSV files are still there.
public final class Plots {

    private final String python;

    // relative to the working directory, e.g. scripts/plots.py
    private final Path script;

    public Plots(final String python, final Path script) {
        this.python = python;
        this.script = script;
    }

    public void draw(final Path results) {
        boolean success;
        try {
            final Process process = new ProcessBuilder(
                this.python, this.script.toString(), results.toString()
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
