import java.util.Comparator;

public class KnapsackChromosomeComparator implements Comparator<KnapsackChromosome> {
    @Override
    public int compare(KnapsackChromosome a, KnapsackChromosome b) {
        int res;
        if (a.fitness > b.fitness) {
            res = -1;
        } else if (a.fitness == b.fitness) {
            res = 0;
        } else {
            res = 1;
        }
        return res;
    }
}
