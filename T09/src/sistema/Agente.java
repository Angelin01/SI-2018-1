package sistema;

import ambiente.Model;
import arvore.TreeNode;
import arvore.fnComparator;
import comuns.Fruta;
import net.sourceforge.jFuzzyLogic.FIS;
import problema.Estado;
import problema.Problema;
import comuns.PontosCardeais;

import java.util.*;

import static java.lang.Math.*;

/**
 *
 * @author tacla
 */
public class Agente implements PontosCardeais {
    // Referência ao ambiente para poder atuar no mesmo
    private Model model;

    // Crença do agente sobre o problema
    private Problema prob;

    // Guarda o estado atual (posição atual do agente)
    private Estado estAtu;

    // Seleciona a heurística inicial para o A* e o LRTA*
    /* 1 - Zero
     * 2 - Distância euclidiana
     * 3 - Distância de Chebyshev (n de movimentos do rei do xadrez)
     */
    private int heuristica;

    // Valores de heurística para cada estado
    private float[][] heurEstados;

    // Soluções ótimas encontradas pelo LRTA*
    List<List<Integer>> solucoesOtimas;
    private double custoOtimo;

    // Variáveis para guardar a última solução computada pelo LRTA*
    private List<Integer> solucaoAtual;
    private String solucaoStr;
    private double custo;

    // Variável utilizada para seguir uma solução conhecida (armazena o índice da próxima ação a executar)
    private int actionIndex;

    // Armazena a energia atual do agente
    private double energy;

    // Contador de iterações
    private static int ct = -1;

    // Gerador de números aleatórios para desempate
    private Random rng;

    // Lógica fuzzy
    private FIS fuzzyson;

    public enum FruitSelection {
        RANDOM,
        ID3,
        FUZZY
    };

    /**
     * Construtor único
     * @param m Referência a um objeto do tipo Model, que representa o ambiente.
     * @param heuristica 0, 1, ou 2. Indica qual heurística será usada pelo A* ou LRTA*.
     */
    public Agente(Model m, int heuristica) {
        // Inicializa a referência ao ambiente e as crenças do agente
        this.model = m;
        prob = new Problema();
        prob.criarLabirinto(9, 9);
        prob.crencaLabir.porParedeHorizontal(1, 2, 8);
        prob.crencaLabir.porParedeVertical(6, 7, 1);
        prob.crencaLabir.porParedeHorizontal(2, 2, 5);
        prob.crencaLabir.porParedeVertical(6, 7, 4);
        prob.crencaLabir.porParedeVertical(5, 6, 5);
        prob.crencaLabir.porParedeVertical(5, 7, 7);
        prob.crencaLabir.porParedeHorizontal(3, 6, 3);
        prob.crencaLabir.porParedeHorizontal(3, 5, 2);
        prob.crencaLabir.porParedeHorizontal(0, 0, 1);
        prob.crencaLabir.porParedeHorizontal(0, 1, 0);
        prob.crencaLabir.porParedeHorizontal(4, 7, 0);
        prob.defEstIni(8, 0);
        prob.defEstObj(2, 6);
        this.estAtu = prob.estIni;

        // Cria um gerador aleatório
        this.rng = new Random();

        // Inicializa as variáveis usadas pelos algoritmos de busca
        this.custo = 0;
        this.heuristica = heuristica;

        // Pré-inicializa a heurística em todas as posições da matriz do LRTA*
        this.heurEstados = new float[prob.crencaLabir.getMaxLin()][prob.crencaLabir.getMaxCol()];
        for (int lin = 0; lin < prob.crencaLabir.getMaxLin(); lin++) {
            for (int col = 0; col < prob.crencaLabir.getMaxCol(); col++) {
                if (heuristica == 1) {
                    heurEstados[lin][col] = heurZero(lin, col);
                }
                else if (heuristica == 2) {
                    heurEstados[lin][col] = heurEuclidiana(lin, col);
                }
                else {
                    heurEstados[lin][col] = heurChebyshev(lin, col);
                }
            }
        }

        // Variáveis utilizadas nas soluções computadas pelo LRTA*
        solucoesOtimas = new ArrayList<>();
        solucaoAtual = new ArrayList<>();
        solucaoStr = "";

        // Executa A* para determinar o custo ótimo
        custoOtimo = determinarCustoOtimo();

        fuzzyson = FIS.load("./fuzzy_frutas.fcl", true);
        if (fuzzyson == null) {
            System.err.println("Não foi possível carregar o arquivo: './fuzzy_frutas.fcl'");
            System.exit(1);
        }
    }
    
    /**
     * Escolhe qual ação (UMA E SOMENTE UMA) será executada em um ciclo de raciocínio.
     * @param verbose Habilita ou desabilita prints.
     * @return 1 enquanto o plano não acabar; -1 quando acabar.
     */
    public int deliberar(boolean verbose) {
        ++ct;

        // Imprime o estado do mundo
        if (verbose) {
            System.out.println("=============\n" +
                               "CT = " + ct + "\n");
            model.desenhar();

            //printHeur();
        }

        if (!prob.testeObjetivo(estAtu)) {
            float menorF = 0;
            float atualF;

            boolean firstIter = true;
            Estado iteradorEstado;
            List<Integer> desempateAcoes = new ArrayList<Integer>();

            // Verifica os estados vizinhos
            int[] acoes = prob.acoesPossiveis(estAtu);

            for (int acao = 0; acao < 8; acao++) {
                if (acoes[acao] != -1) {
                    // Pega um estado vizinho
                    iteradorEstado = prob.suc(estAtu, acao);

                    // Calcula o f(n')
                    atualF = heurEstados[iteradorEstado.getLin()][iteradorEstado.getCol()];
                    if (acao % 2 == 0) atualF += 1.0f;
                    else atualF += 1.5f;

                    // Se é a primeira iteração, seta o valor do menor f(n')
                    if (firstIter) {
                        firstIter = false;
                        menorF = atualF;
                        desempateAcoes.add(acao);
                    }
                    // Se não é a primeira iteração, compara o f(n') atual
                    else {
                        if (atualF < menorF) {
                            menorF = atualF;
                            desempateAcoes.clear();
                            desempateAcoes.add(acao);
                        } else if (atualF == menorF) {
                            desempateAcoes.add(acao);
                        }
                    }
                }
            }

            // Atualiza a heurística do estado atual
            heurEstados[estAtu.getLin()][estAtu.getCol()] = menorF;

            // Escolhe a ação que leva a um vizinho aleatório com o menor f(n')
            int acaoAFazer = desempateAcoes.get((rng.nextInt(desempateAcoes.size())));

            if (verbose) {
                System.out.println("Estado atual: (" + estAtu.getLin() + "," + estAtu.getCol() + ")");
                System.out.println("Custo parcial: " + custo);
                System.out.print("Acoes possiveis: ");
                for (int i = 0; i < 8; i++) {
                    if (acoes[i] != -1) {
                        System.out.print(acao[i] + " ");
                    }
                }
                System.out.println("\nProxima acao (escolhida): " + acao[acaoAFazer] + "\n");
            }

            custo += (acaoAFazer % 2 == 0) ? 1.0f : 1.5f;
            solucaoStr += " " + acao[acaoAFazer];
            solucaoAtual.add(acaoAFazer);
            executarIr(acaoAFazer);

            // atualiza estado atual - sabendo que o ambiente eh deterministico
            estAtu = prob.suc(estAtu, acaoAFazer);
        }
        else {
            if (verbose) {
                System.out.println("Fim.");
                System.out.println("Estado atual: (" + estAtu.getLin() + "," + estAtu.getCol() + ")");
                System.out.println("Custo final: " + custo);
                System.out.println("Razao de competitividade: " + custo + "/" + Double.toString(custoOtimo) + " = " + custo/custoOtimo);
            }

            if(custo == custoOtimo) {
                if (verbose) System.out.println("!!! Solucao otima encontrada !!!: " + solucaoStr);

                boolean newSolution = true;
                for (List<Integer> list : solucoesOtimas) {
                    if (list.equals(solucaoAtual)) {
                        newSolution = false;
                        break;
                    }
                }

                if (newSolution) {
                    solucoesOtimas.add(solucaoAtual);
                }
            }

            return -1;
        }

        return 1;
    }

    /**
     * Seleciona uma solução aleatória pré-gerada pelo LRTA* para seguir usando followKnownSolution.
     */
    public void selectRandomSolution() {
        solucaoAtual = solucoesOtimas.get(rng.nextInt(solucoesOtimas.size()));
        actionIndex = 0;
        energy = 3;
    }

    /**
     * Seleciona uma solução aleatória pré-gerada pelo LRTA* para seguir usando followKnownSolution.
     * @param selection Tipo de escolha de frutas (enum FruitSelection)
     * @param verbose Habilita ou desabilita prints
     * @return 1 enquanto ainda houver ações a fazer; -1 quando acabar.
     */
    public int followKnownSolution(FruitSelection selection, boolean verbose) {
        if (verbose) System.out.println("Estou na posição " + estAtu.getString() + " com " + energy + " de energy.");

        // Se o agente ainda tiver energy e ainda houver ações a executar no plano
        if (energy >= 0 && actionIndex < solucaoAtual.size()) {
            if (!prob.testeObjetivo(estAtu)) {
                // Decide se come a fruta na posição atual ou não
                if (selection == FruitSelection.RANDOM) {
                    if (randomFruitRoll()) {
                        Fruta frutaPosAtual = model.getFrutaPos();
                        energy += energia(frutaPosAtual.getCor());
                        if (verbose) System.out.println("Decidi comer a fruta na minha posição. Agora minha energy é " + energy + ".");
                    }
                    else if (verbose) {
                        System.out.println("Decidi não comer a fruta na minha posição.");
                    }
                }
                else if (selection == FruitSelection.ID3) {
                    Fruta frutaPosAtual = model.getFrutaPos();
                    if (logicalFruitSelection(frutaPosAtual.getCor())) {
                        energy += energia(frutaPosAtual.getCor());
                        if (verbose) System.out.println("Decidi comer a fruta na minha posição. Agora minha energy é " + energy + ".");
                    }
                    else if (verbose) {
                        System.out.println("Decidi não comer a fruta na minha posição.");
                    }
                }
                else {
                    Fruta frutaPosAtual = model.getFrutaPos();
                    if (fuzzyFruitSelection(frutaPosAtual.getCor())) {
                        energy += energia(frutaPosAtual.getCor());
                        if (verbose) System.out.println("Decidi comer a fruta na minha posição. Agora minha energy é " + energy + ".");
                    }
                    else if (verbose) {
                        System.out.println("Decidi não comer a fruta na minha posição.");
                    }
                }
            }

            int action = solucaoAtual.get(actionIndex);
            if (verbose) System.out.println("Farei a ação " + acao[action]);

            energy -= (action % 2 == 0) ? 1.0f : 1.5f;
            executarIr(action);
            estAtu = prob.suc(estAtu, action);

            ++actionIndex;

            if (verbose) System.out.println("Agora estou na posição " + estAtu.getString() + " com " + energy + " de energy.\n");

            return 1;
        }

        if (energy < 0) {
            if (verbose) System.out.println("Morri. Pontuação: -100");
            energy = 100;
        }
        else if (verbose && prob.testeObjetivo(estAtu)) {
            System.out.println("Estou no objetivo. Pontuação: " + energy *(-1));
        }

        return -1;
    }

    /**
     * Retorna a quantidade de energia que o agente possui no momento
     * @return Quantidade de energia que o agente possui nesse momento
     */
    public double getEnergy() {
        return energy;
    }

    /**
     * Faz um lance aleatório para decidir se come uma fruta ou não, de acordo com uma porcentagem definida.
     * @return true, se a fruta deve ser comida; false, caso contrário.
     */
    private boolean randomFruitRoll() {
        return (rng.nextDouble() < 0.7f);
    }

    /**
     * Decide se come ou não uma fruta com base em suas cores.
     * @param cor Vetor de 5 posições contendo as cores da fruta.
     * @return true, se a fruta deve ser comida; false, caso contrário.
     */
    private boolean logicalFruitSelection(char[] cor) {
        // Check de entrada de tamanho inválido
        if (cor.length != 5) {
            return false;
        }

        // Valor de energia fornecido pela fruta
        int fruitEnergy = energia(cor);

        // Quantidade de energia atual
        // energy

        // Quanto estima gastar
        float energyNeeded = 0;
        for (int i = actionIndex; i < solucaoAtual.size(); ++i) {
            if (solucaoAtual.get(i) % 2 == 0) {
                energyNeeded += 1.0f;
            }
            else {
                energyNeeded += 1.5f;
            }
        }

        return (fruitEnergy != 0 && energy < energyNeeded && (energy + fruitEnergy) < (energyNeeded + 2));
    }

    private boolean fuzzyFruitSelection(char[] cor) {
        // Check de entrada de tamanho inválido
        if (cor.length != 5) {
            return false;
        }

        // Valor de energia fornecido pela fruta
        int fruitEnergy = energia(cor);

        // Quantidade de energia atual
        // energy

        // Quanto estima gastar
        float energyNeeded = 0;
        for (int i = actionIndex; i < solucaoAtual.size(); ++i) {
            if (solucaoAtual.get(i) % 2 == 0) {
                energyNeeded += 1.0f;
            }
            else {
                energyNeeded += 1.5f;
            }
        }

        fuzzyson.setVariable("energia_atual", energy);
        fuzzyson.setVariable("energia_fruta", (double) fruitEnergy);
        fuzzyson.setVariable("estima_gastar", energyNeeded);
        fuzzyson.evaluate();

        //System.out.println("energy: " + energy + ", fruitEnergy: " + fruitEnergy + ", energyNeeded: " + energyNeeded + ", result: " + fuzzyson.getVariable("deseja_comer").getValue());

        return fuzzyson.getVariable("deseja_comer").getValue() > 0.5f;
    }
    
    /**
     * Funciona como um driver ou um atuador: envia o comando para
     * agente físico ou simulado (no nosso caso, simulado)
     * @param direcao N NE S SE ...
     * @return 1 se ok ou -1 se falha
     */
    public int executarIr(int direcao) {
        model.ir(direcao);
        return 1; 
    }

    /**
     * Retorna a posição atual do agente em formato de objeto Estado.
     * @return objeto do tipo Estado contendo as coordenadas do agente.
     */
    public Estado sensorPosicao() {
        int pos[];
        pos = model.lerPos();
        return new Estado(pos[0], pos[1]);
    }

    /**
     * Executa o algoritmo A* para determinar o custo ótimo até o objetivo.
     * @return valor do menor custo de caminho do estado inicial até o objetivo.
     */
    private float determinarCustoOtimo() {
        // Cria a raiz da árvore
        TreeNode noInicial = new TreeNode(null);
        noInicial.setAction(-1);
        noInicial.setState(prob.estIni);
        noInicial.setGn(0);
        noInicial.setHn(
                heuristica == 1 ?
                    heurZero(prob.estIni.getLin(), prob.estIni.getCol())
                : heuristica == 2 ?
                    heurEuclidiana(prob.estIni.getLin(), prob.estIni.getCol())
                :
                    heurChebyshev(prob.estIni.getLin(), prob.estIni.getCol())
        );

        // Usando listas/queues em vez de arrays pq eh mais apropriado
        Queue<TreeNode> fronteira = new PriorityQueue<TreeNode>(new fnComparator());
        List<Estado> explorados = new ArrayList<Estado>();

        fronteira.add(noInicial);

        TreeNode noAtual;
        Estado estadoAtual;

        while (true) {
            if (fronteira.size() == 0) {
                return -1; // Falhou a procura
            }

            // Retira o elemento de menor f(n)
            noAtual = fronteira.poll();
            estadoAtual = noAtual.getState();

            // Testa se chegou ao objetivo. Se sim, monta o vetor solução e retorna.
            if(prob.testeObjetivo(estadoAtual)) {
                int[] plan = new int[noAtual.getDepth()];
                float cost = 0;

                TreeNode nodeIterator = noAtual;
                for (int i = noAtual.getDepth() - 1; i >= 0; --i) {
                    plan[i] = nodeIterator.getAction();
                    cost += (plan[i] % 2 == 0) ? 1.0f : 1.5f;
                    nodeIterator = nodeIterator.getParent();
                }

                return cost;
            }

            explorados.add(estadoAtual);

            int[] acoes = prob.acoesPossiveis(estadoAtual);
            for (int acao = 0; acao < 8; ++acao) {
                if (acoes[acao] != -1) { // Para cada possivel acao...
                    // Cria um nó na árvore
                    TreeNode filho = noAtual.addChild();
                    filho.setState(prob.suc(estadoAtual, acao));
                    filho.setAction(acao);
                    filho.setGn((noAtual.getGn() + (acao % 2 == 0 ? 1.0f : 1.5f))); // Adiciona custo 1 se for acao N S L O, senao 1.5
                    filho.setHn(
                            heuristica == 1 ?
                                heurZero(filho.getState().getLin(), filho.getState().getCol())
                            : heuristica == 2 ?
                                heurEuclidiana(filho.getState().getLin(), filho.getState().getCol())
                            :
                                heurChebyshev(filho.getState().getLin(), filho.getState().getCol())
                    );

                    TreeNode noAntigo;
                    if (!filho.getState().contidoEm(explorados)) {
                        noAntigo = filho.contidoEm(fronteira);
                        if (noAntigo == null) {
                            // Se o estado não foi explorado, e o nó não está na fronteira, adiciona-o à fronteira.
                            fronteira.add(filho);
                        }
                        else {
                            // Se o estado não foi explorado, e o nó já está na fronteira, compara os custos.
                            if (noAntigo.getFn() > filho.getFn()) {
                                fronteira.add(filho);
                                fronteira.remove(noAntigo);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retorna sempre zero.
     * Independe dos parâmetros: eles estão aí somente porque tínhamos pensado
     * em fazer algo como C++, e passar a função de heurística escolhida
     * como parâmetro. Aí precisaríamos que todas as funções de heurística
     * tivessem a mesma assinatura.
     * Mas não conseguimos fazer isso em Java.
     * @return 0.0f sempre.
     */
    private float heurZero(int lin, int col) {
        return 0.0f;
    }

    /**
     * Retorna a distância euclidiana entre uma posição qualquer e a posição
     * do objetivo.
     * A distância euclidiana é a distância em linha reta, calculada pelo
     * Teorema de Pitágoras.
     * @param lin Coordenada Y
     * @param col Coordenada X
     * @return Distância euclidiana entre o ponto e o objetivo.
     */
    private float heurEuclidiana(int lin, int col) {
        return (float) sqrt(
                (prob.estObj.getLin() - lin) * (prob.estObj.getLin() - lin) +
                        (prob.estObj.getCol() - col) * (prob.estObj.getCol() - col)
        );
    }

    /**
     * Retorna a distância de Chebyshev entre uma posição qualquer e a posição
     * do objetivo.
     * A distância de Chebyshev é o menor número de movimentos do Rei do xadrez
     * que o levam do primeiro ponto ao segundo ponto.
     * Em outras palavras, é o maior número entre a distância entre linhas e a
     * distância entre colunas.
     * @param lin Coordenada Y
     * @param col Coordenada X
     * @return Distância euclidiana entre o ponto e o objetivo.
     */
    private float heurChebyshev(int lin, int col) {
        return (float) max(
                abs(prob.estObj.getLin() - lin),
                abs(prob.estObj.getCol() - col)
        );
    }

    /**
     * Retorna o custo da solução atual.
     * @return custo da solução atual.
     */
    public double getCusto() {
        return custo;
    }

    /**
     * Retorna o custo da solução ótima.
     * @return custo da solução ótima.
     */
    public double getCustoOtimo() {
        return custoOtimo;
    }

    /**
     * Reinicia o agente, para que seja possível fazer uma nova execução
     * sem criar uma nova instância.
     */
    public void reset() {
        custo = 0;
        ct = -1;
        estAtu = prob.estIni;
        solucaoStr = "";
        solucaoAtual = new ArrayList<>();
    }

    /**
     * Mostra a matriz de heurísticas na saída padrão.
     */
    public void printHeur() {
        // Pré-inicializa a heurística em todas as posições
        for (int lin = 0; lin < prob.crencaLabir.getMaxLin(); lin++) {
            for (int col = 0; col < prob.crencaLabir.getMaxCol(); col++) {
                System.out.print("+------");
            }
            System.out.println("+");
            for (int col = 0; col < prob.crencaLabir.getMaxCol(); col++) {
                System.out.print("| ");
                System.out.printf("%.2f", heurEstados[lin][col]);
                System.out.print(" ");
            }
            System.out.println("|");
        }
        for (int col = 0; col < prob.crencaLabir.getMaxCol(); col++) {
            System.out.print("+------");
        }
        System.out.println("+");
    }

    /**
     * Determina a energia de uma fruta, com base em suas características de cor.
     * A lógica dessa função é baseada na árvore de decisão gerada pelo algoritmo
     * ID3, com base no dataset fornecido.
     * @param cor Vetor de 5 posições contendo as características de cor
     * @return Energia da fruta.
     */
    private int energia(char[] cor) {
        // Check de entrada de tamanho inválido
        if (cor.length != 5) {
            return -1;
        }

        // Árvore de decisão de acordo com ID3
        if (cor[1] == 'R') {
            if (cor[3] == 'R') {
                if (cor[2] == 'R') {
                    return 4;
                }
                else if (cor[2] == 'G') {
                    return 2;
                }
                else if (cor[2] == 'B') {
                    return 2;
                }
            }
            if (cor[3] == 'G') {
                if (cor[2] == 'R') {
                    return 2;
                }
                else if (cor[2] == 'G') {
                    return 2;
                }
                else if (cor[2] == 'B') {
                    return 0;
                }
            }
            if (cor[3] == 'B') {
                if (cor[2] == 'R') {
                    return 2;
                }
                else if (cor[2] == 'G') {
                    return 4;
                }
                else if (cor[2] == 'B') {
                    return 2;
                }
            }
        }
        else if (cor[1] == 'G') {
            if (cor[0] == 'K') {
                if (cor[3] == 'R') {
                    if (cor[2] == 'R') {
                        return 2;
                    }
                    else if (cor[2] == 'G') {
                        return 2;
                    }
                    else if (cor[2] == 'B') {
                        return 0;
                    }
                }
                if (cor[3] == 'G') {
                    if (cor[2] == 'R') {
                        return 2;
                    }
                    else if (cor[2] == 'G') {
                        return 4;
                    }
                    else if (cor[2] == 'B') {
                        return 2;
                    }
                }
                if (cor[3] == 'B') {
                    if (cor[2] == 'R') {
                        return 0;
                    }
                    else if (cor[2] == 'G') {
                        return 2;
                    }
                    else if (cor[2] == 'B') {
                        return 2;
                    }
                }
            }
            else if (cor[0] == 'W') {
                return 0;
            }
        }
        else if (cor[1] == 'B') {
            if (cor[2] == 'R') {
                if (cor[3] == 'R') {
                    return 2;
                }
                else if (cor[3] == 'G') {
                    return 0;
                }
                else if (cor[3] == 'B') {
                    return 2;
                }
            }
            if (cor[2] == 'G') {
                if (cor[3] == 'R') {
                    return 0;
                }
                else if (cor[3] == 'G') {
                    return 2;
                }
                else if (cor[3] == 'B') {
                    return 2;
                }
            }
            if (cor[2] == 'B') {
                if (cor[3] == 'R') {
                    return 2;
                }
                else if (cor[3] == 'G') {
                    return 2;
                }
                else if (cor[3] == 'B') {
                    return 4;
                }
            }
        }

        // Se algum char não tiver no conjunto certo
        return -1;
    }
}
