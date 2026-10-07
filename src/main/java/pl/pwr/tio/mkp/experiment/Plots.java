package pl.pwr.tio.mkp.experiment;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Draws plots and summary tables by calling the Python script {@code scripts/plots.py}.
 * Plotting is part of the research, not of the algorithm, so it may use libraries
 * (pandas, matplotlib). When Python is not available the CSV files are still there.
 */
public final class Plots {

    /**
     * Path of the script, relative to the working directory.
     */
    private static final Path SCRIPT = Path.of("scripts", "plots.py");

    /**
     * Command starting Python.
     */
    private final String python;

    /**
     * Ctor.
     *
     * @param python Command starting Python, for example {@code python} or {@code python3}
     */
    public Plots(final String python) {
        this.python = python;
    }

    /**
     * Runs the script on a directory with results.
     *
     * @param results Directory with one subdirectory per instance
     * @return True if the script finished successfully
     */
    public boolean draw(final Path results) {
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
        return success;
    }
}
