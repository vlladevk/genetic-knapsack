package pl.pwr.tio.mkp.experiment;

import java.util.List;
import pl.pwr.tio.mkp.operator.BitFlipMutator;
import pl.pwr.tio.mkp.operator.GeneticOperator;
import pl.pwr.tio.mkp.operator.InversionMutator;

// Mutation operators compared in the experiments; BOTH = bit-flip, then inversion.
public enum MutationType {
    BIT_FLIP,
    INVERSION,
    BOTH;

    // perGene: pm of bit-flip; perChromosome: p of inversion
    public List<GeneticOperator> create(final double perGene, final double perChromosome) {
        return switch (this) {
            case BIT_FLIP -> List.of(new BitFlipMutator(perGene));
            case INVERSION -> List.of(new InversionMutator(perChromosome));
            case BOTH -> List.of(new BitFlipMutator(perGene),
                new InversionMutator(perChromosome));
        };
    }
}
