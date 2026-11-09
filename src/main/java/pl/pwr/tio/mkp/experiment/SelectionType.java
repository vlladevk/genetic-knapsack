package pl.pwr.tio.mkp.experiment;

import pl.pwr.tio.mkp.selector.RankSelector;
import pl.pwr.tio.mkp.selector.RouletteSelector;
import pl.pwr.tio.mkp.selector.Selector;
import pl.pwr.tio.mkp.selector.TournamentSelector;

// Selection methods compared in the experiments.
// Tournament k = 2 and rank s = 2 have about the same pressure: in both the best
// individual is picked about twice as often as the average one.
public enum SelectionType {
    TOURNAMENT,
    ROULETTE,
    RANK;

    private static final int TOURNAMENT_SIZE = 2;

    private static final double RANK_PRESSURE = 2.0;

    public Selector create() {
        return switch (this) {
            case TOURNAMENT -> new TournamentSelector(SelectionType.TOURNAMENT_SIZE);
            case ROULETTE -> new RouletteSelector();
            case RANK -> new RankSelector(SelectionType.RANK_PRESSURE);
        };
    }
}
