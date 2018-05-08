import java.util.*;

public class KnapsackProblem {
    private static final int MAX_GENERATIONS = 1000;
    private static final int POPULATION_SIZE = 64;
    private static final int REPRODUCTION_SIZE = 64;

    private static final double CROSSOVER_PROB = 0.8;
    private static final double MUTATION_PROB = 0.05;

    public boolean repairWhenInvalid; // true para reparar, false para penalizar

    public KnapsackItem[] itemSet;
    public int capacity;

    public Random rng;

    private ArquivoTexto log;

    public KnapsackProblem(int capacity, int[] value, int[] weight, boolean repairWhenInvalid, ArquivoTexto file) {
        if (value.length == weight.length) {
            itemSet = new KnapsackItem[value.length];
            this.capacity = capacity;
            this.log = file;
            this.repairWhenInvalid = repairWhenInvalid;

            for (int i = 0; i < itemSet.length; ++i) {
                itemSet[i] = new KnapsackItem(value[i], weight[i]);
            }

            rng = new Random();
        }
    }

    public KnapsackChromosome solve() {
        KnapsackChromosome bestChromo = null;

        //System.out.println("*** Iniciando geração inicial ***");

        // Gera população inicial
        List<KnapsackChromosome> population = new ArrayList<>(POPULATION_SIZE);
        for (int i = 0; i < POPULATION_SIZE; i++) {
            KnapsackChromosome chromo = new KnapsackChromosome(this, rng);
            chromo.randomize();
            chromo.fitness = getFitness(chromo);

            int chromoWeight = getTotalWeight(chromo);
            if (chromoWeight > capacity) {
                if (repairWhenInvalid) {
                    chromo.repair(chromoWeight, capacity);
                } else {
                    chromo.applyPenalty();
                }
            }

            //chromo.print();

            /*
            int chromoWeight = getTotalWeight(chromo);
            if (chromoWeight > capacity) {
                if (repairWhenInvalid) {
                    chromo.repair(chromoWeight, capacity);
                    System.out.print("Reparo: ");
                    chromo.print();
                }
                else {
                    chromo.applyPenalty();
                    System.out.print("Penalidade: ");
                    chromo.print();
                }
            }
            */

            population.add(chromo);
        }

        int generation = 0;

        do {
            /*
            // DEBUG
            System.out.println("\n*** Geração " + generation + "***");
            System.out.println("População: ");
            for (int j = 0; j < POPULATION_SIZE; j++) {
                population.get(j).print();
            }*/

            // Calcula a soma de todos os fitnesses para fazer o sorteio por roleta
            int sumFitnesses = 0;
            for (KnapsackChromosome chromo : population) {
                sumFitnesses += chromo.fitness;
            }

            // Seleciona os cromossomos de acordo com seu fitness
            List<KnapsackChromosome> reproduction = new ArrayList<>(REPRODUCTION_SIZE);
            for (int j = 0; j < REPRODUCTION_SIZE; j++) {
                reproduction.add(new KnapsackChromosome(rouletteSelect(population, sumFitnesses)));
            }

            /*System.out.println("Reprodução original: ");
            for (int j = 0; j < POPULATION_SIZE; j++) {
                reproduction.get(j).print();
            }*/

            // Faz o crossover
            for (int j = 0; j < REPRODUCTION_SIZE; j+=2) {
                if (rng.nextDouble() < CROSSOVER_PROB) {
                    for (int i = rng.nextInt(itemSet.length - 1) + 1; i < itemSet.length; i++) {
                        boolean swap = reproduction.get(j).isInKnapsack[i];
                        reproduction.get(j).isInKnapsack[i] = reproduction.get(j+1).isInKnapsack[i];
                        reproduction.get(j+1).isInKnapsack[i] = swap;
                    }
                }
            }

            /*System.out.println("Reprodução após crossover: ");
            for (int j = 0; j < POPULATION_SIZE; j++) {
                reproduction.get(j).print();
            }*/

            // Faz mutação
            for (int j = 0; j < REPRODUCTION_SIZE; j++) {
                for (int i = 0; i < itemSet.length; i++) {
                    if (rng.nextDouble() < MUTATION_PROB) {
                        reproduction.get(j).isInKnapsack[i] = !reproduction.get(j).isInKnapsack[i];
                    }
                }
            }

            /*System.out.println("Reprodução após mutação: ");
            for (int j = 0; j < POPULATION_SIZE; j++) {
                reproduction.get(j).print();
            }*/

            // Calcula o novo fitness dos filhos
            for (int j = 0; j < REPRODUCTION_SIZE; j++) {
                reproduction.get(j).fitness = getFitness(reproduction.get(j));
            }

            // Penaliza ou repara soluções infactíveis
            for (int j = 0; j < REPRODUCTION_SIZE; j++) {
                KnapsackChromosome chromo = reproduction.get(j);
                int chromoWeight = getTotalWeight(chromo);
                if (chromoWeight > capacity) {
                    if (repairWhenInvalid) {
                        //System.out.println("Infactível: f: " + chromo.fitness + ", w: " + chromoWeight);
                        chromo.repair(chromoWeight, capacity);
                        //System.out.println("Reparado: f: " + chromo.fitness + ", w: " + getTotalWeight(chromo));
                    } else {
                        //System.out.println("Infactível: f: " + chromo.fitness + ", w: " + chromoWeight);
                        chromo.applyPenalty();
                        //System.out.println("Penalizado: f: " + chromo.fitness + ", w: " + getTotalWeight(chromo));
                    }
                }
            }

            /*System.out.println("Reprodução após penalizações/reparações: ");
            for (int j = 0; j < POPULATION_SIZE; j++) {
                reproduction.get(j).print();
            }*/

            // Joga todos numa lista ordenada
            Queue<KnapsackChromosome> listaMelhores = new PriorityQueue<>(new KnapsackChromosomeComparator());
            for (int j = 0; j < POPULATION_SIZE; j++) {
                listaMelhores.add(population.get(j));
            }
            for (int j = 0; j < REPRODUCTION_SIZE; j++) {
                listaMelhores.add(reproduction.get(j));
            }

            // Se o melhor da lista for o melhor de todas as gerações, guarda ele
            if (bestChromo == null || bestChromo.fitness < listaMelhores.peek().fitness) {
                bestChromo = listaMelhores.peek();
            }

            // Retira os POPULATION_SIZE melhores e coloca na população
            population.clear();
            for (int j = 0; j < POPULATION_SIZE; j++) {
                population.add(listaMelhores.poll());
            }

            // Escreve no arquivo
            log.appendTextWithNewLine(String.valueOf(generation) + "," + String.valueOf(bestChromo.fitness));

            generation++;
        } while (generation < MAX_GENERATIONS);

        return bestChromo;
    }

    private KnapsackChromosome rouletteSelect(List<KnapsackChromosome> population, int sumFitnesses) {
        double roulette = rng.nextDouble() * sumFitnesses;

        for (int i = 0; i < POPULATION_SIZE; i++) {
            roulette -= population.get(i).fitness;
            if (roulette < 0) {
                return population.get(i);
            }
        }

        // Caso passe por todos os elementos, só retorna o último
        return population.get(POPULATION_SIZE - 1);
    }

    private int getFitness(KnapsackChromosome chromo) {
        int fitness = 0;
        for (int i = 0; i < itemSet.length; i++) {
            if (chromo.isInKnapsack[i]) {
                fitness += itemSet[i].value;
            }
        }

        return fitness;
    }

    public int getTotalWeight(KnapsackChromosome chromo) {
        int weight = 0;
        for (int i = 0; i < itemSet.length; i++) {
            if (chromo.isInKnapsack[i]) {
                weight += itemSet[i].weight;
            }
        }
        return weight;
    }
}
