import java.util.Random;

public class KnapsackChromosome {
    protected KnapsackProblem problem;

    public boolean[] isInKnapsack;

    public int fitness;

    private Random rng;

    public KnapsackChromosome(KnapsackProblem problem, Random rng) {
        this.problem = problem;
        this.isInKnapsack = new boolean[problem.itemSet.length];
        this.fitness = -1;
        this.rng = rng;
    }

    // Copia um cromossomo
    public KnapsackChromosome(KnapsackChromosome chromo) {
        this.problem = chromo.problem;
        this.isInKnapsack = new boolean[this.problem.itemSet.length];
        System.arraycopy(chromo.isInKnapsack, 0, this.isInKnapsack, 0, this.isInKnapsack.length);
        this.fitness = chromo.fitness;
        this.rng = chromo.rng;
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

    public void printTable() {
        int itemCount = 0, totalWeight = 0;
        System.out.println("-----------------------");
        System.out.println(" Mochila | Peso | Valor\n" +
                           "---------+------+------");

        for (int i = 0; i < isInKnapsack.length; ++i) {
            if (isInKnapsack[i]) {
                ++itemCount;
                totalWeight += problem.itemSet[i].weight;
                System.out.println(String.format("item [%02d]|%6d|%5d", i+1, problem.itemSet[i].weight, problem.itemSet[i].value));
            }
        }

        System.out.println("-----------------------");
        System.out.println(String.format("Mochila com %3d ITENS", itemCount));
        System.out.println(String.format("Mochila com %3d KG", totalWeight));
        System.out.println(String.format("Mochila com %3d VALOR", fitness));
        System.out.println("-----------------------");
    }

    public void randomize() {
        for (int i = 0; i < isInKnapsack.length; ++i) {
            isInKnapsack[i] = (rng.nextDouble() < 0.2);
        }
    }
}
