public class KnapsackChromosome {
    protected KnapsackProblem problem;

    public boolean[] isInKnapsack;

    public int fitness;

    public KnapsackChromosome(KnapsackProblem problem) {
        this.problem = problem;
        this.isInKnapsack = new boolean[problem.itemSet.length];
        this.fitness = -1;
    }

    // Copia um cromossomo
    public KnapsackChromosome(KnapsackChromosome chromo) {
        this.problem = chromo.problem;
        this.isInKnapsack = new boolean[this.problem.itemSet.length];
        System.arraycopy(chromo.isInKnapsack, 0, this.isInKnapsack, 0, this.isInKnapsack.length);
        this.fitness = chromo.fitness;
    }

    // Função bem "burra" de penalização
    public void applyPenalty() {
        this.fitness /= 6;
    }

    // Função bem "burra" de reparo
    public void repair(int presentWeight, int capacity) {
        for (int i = 0; i < isInKnapsack.length; i++) {
            if (isInKnapsack[i]) {
                isInKnapsack[i] = false;
                fitness -= problem.itemSet[i].value;
                presentWeight -= problem.itemSet[i].weight;
                if (presentWeight <= capacity) {
                    return;
                }
            }
        }
    }

    public void print() {
        for (int i = 0; i < isInKnapsack.length; i++) {
            if (isInKnapsack[i]) {
                System.out.print("1 ");
            }
            else {
                System.out.print("0 ");
            }
        }
        System.out.println(" (f: " + fitness + ")");
    }
}
