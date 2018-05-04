import java.util.Scanner;

public class KnapsackMain {
    // Valores dos itens do problema
    private static final int[] value = {
            1,  3,  1,  8,  9,  3,  2,  8,  5,  1,  1,  6,  3,  2,
            5,  2,  3,  8,  9,  3,  2,  4,  5,  4,  3,  1,  3,  2,
            14, 32, 20, 19, 15, 37, 18, 13, 19, 10, 15, 40, 17, 39
    };

    // Pesos dos itens do problema
    private static final int[] weight = {
             3,  8, 12,  2,  8,  4,  4,  5,  1,  1,  8,  6,  4,  3,
             3,  5,  7,  3,  5,  7,  4,  3,  7,  2,  3,  5,  4,  3,
             7, 19, 20, 21, 11, 24, 13, 17, 18,  6, 15, 25, 12, 19
    };

    // Capacidade da mochila
    private static final int knapsackCapacity = 113;

    // Número de execuções
    private static final int executions = 1000;

    private static ArquivoTexto logExecucoes;
    private static ArquivoTexto logGeracoes;

    // Configurações do algoritmo estão na classe KnapsackProblem!!

    public static void main(String[] args) {
        logExecucoes = new ArquivoTexto("log_execucoes.csv");
        logGeracoes  = new ArquivoTexto("log_geracoes.csv");

        Scanner in = new Scanner(System.in);
        int metodo = 0;

        while(metodo != 1 && metodo != 2) {
            System.out.println("Escolha um método:\n" +
                    "1 - Reparar\n" +
                    "2 - Penalizar");
            metodo = in.nextInt();
        }

        KnapsackProblem problem;
        // Cria a instância do problema
        if (metodo == 1) {
            problem = new KnapsackProblem(knapsackCapacity, value, weight, true, logGeracoes);
        }
        else if (metodo == 2) {
            problem = new KnapsackProblem(knapsackCapacity, value, weight, false, logGeracoes);
        }
        else {
            System.out.println("Método inválido identificado. Selecionado \"Reparar\"");
            problem = new KnapsackProblem(knapsackCapacity, value, weight, true, logGeracoes);
        }

        KnapsackChromosome bestSolution = null;
        for (int execCount = 0; execCount < 1000; execCount++) {
            KnapsackChromosome solution = problem.solve();

            if(bestSolution == null || bestSolution.fitness < solution.fitness) {
                bestSolution = solution;
            }

            int itemCount = 0;

            for (int i = 0; i < problem.itemSet.length; i++) {
                if (solution.isInKnapsack[i]) {
                    itemCount++;
                }
            }
            logExecucoes.appendText(String.valueOf(itemCount) + ","
                                  + String.valueOf(problem.getTotalWeight(solution)) + ","
                                  + String.valueOf(solution.fitness) + ",");
            for (int i = 0; i < problem.itemSet.length; i++) {
                if (solution.isInKnapsack[i]) {
                    logExecucoes.appendText("1");
                }
                else {
                    logExecucoes.appendText("0");
                }
                if (i != problem.itemSet.length - 1) {
                    logExecucoes.appendText(",");
                }
            }
            logExecucoes.appendText("\n");


            solution.print();

            /*for (int i = 0; i < problem.itemSet.length; i++) {
                if (solution.isInKnapsack[i]) {
                    System.out.println("Item " + i + ": Valor " + problem.itemSet[i].value + ", Peso " + problem.itemSet[i].weight);
                }
            }*/
        }

        bestSolution.printTable();

        logExecucoes.closeFile();
        logGeracoes.closeFile();
    }
}
