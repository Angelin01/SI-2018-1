package sistema;

import ambiente.Model;
import problema.Estado;
import problema.Problema;
import comuns.PontosCardeais;
import arvore.TreeNode;
import arvore.fnComparator;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

/**
 *
 * @author tacla
 */
public class Agente implements PontosCardeais {
    /* referência ao ambiente para poder atuar no mesmo*/
    Model model;
    Problema prob;
    Estado estAtu; // guarda o estado atual (posição atual do agente)

    int plan[];
    double custo;
    static int ct = -1;
    int algoritmo; /* Algoritmo escolhido
                    1 - Custo Uniforme
                    2 - A* com distancia euclidiana
                    3 - A* com distancia chebyshev */

    static String[] nomeAlgoritmo = {"Custo Uniforme", "A* (Euclidiana)", "A* (Chebyshev)"};

    int countIterGenPlano = 0;        // Quantidade de iterações do algoritmo de pathfinding
    int countNosTotal = 0;            // Quantidade total de nós na árvore
    int countNosJaExpl = 0;           // Quantidade de nós não inseridos na fronteira por já terem sido explorados
    int countNosDescartadosFront = 0; // Quantidade de nós não inseridos ou descartados na comparação de f(n) na fronteira
           
    public Agente(Model m, int algoritmo) {
        this.model = m;
        prob = new Problema();
        prob.criarLabirinto(9, 9);
        prob.crencaLabir.porParedeVertical(0, 1, 0);
        prob.crencaLabir.porParedeVertical(0, 0, 1);
        prob.crencaLabir.porParedeVertical(5, 8, 1);
        prob.crencaLabir.porParedeVertical(5, 5, 2);
        prob.crencaLabir.porParedeVertical(8, 8, 2);
        prob.crencaLabir.porParedeHorizontal(4, 7, 0);
        prob.crencaLabir.porParedeHorizontal(7, 7, 1);
        prob.crencaLabir.porParedeHorizontal(3, 5, 2);
        prob.crencaLabir.porParedeHorizontal(3, 5, 3);
        prob.crencaLabir.porParedeHorizontal(7, 7, 3);
        prob.crencaLabir.porParedeVertical(6, 7, 4);
        prob.crencaLabir.porParedeVertical(5, 6, 5);
        prob.crencaLabir.porParedeVertical(5, 7, 7);
        
        // Estado inicial, objetivo e atual
        prob.defEstIni(8, 0);
        prob.defEstObj(2, 8);
        this.estAtu = prob.estIni;
        this.custo = 0;

        this.algoritmo = algoritmo;
    }
    
    /**Escolhe qual ação (UMA E SOMENTE UMA) será executada em um ciclo de raciocínio
     * @return 1 enquanto o plano não acabar; -1 quando acabar
     */
    public int deliberar() {
        if(++ct == 0) { // Primeiro ciclo
            // Executa o algoritmo de busca e armazena a solução
            plan = gerarPlano();

            // Imprime a solução
            System.out.print("=============\n" +
                    "GERACAO DO PLANO\n" +
                    "Algoritmo: " + nomeAlgoritmo[algoritmo-1] + "\n" +
                    "Iteracoes: " + countIterGenPlano + "\n" +
                    "Arvore: total de " + countNosTotal + " nos; Destes:\n" +
                    "    " + countNosJaExpl + " nao inseridos na fronteira por ja terem sido explorados.\n" +
                    "    " + countNosDescartadosFront + " descartados na comparacao de custos na fronteira.\n" +
                    "Solucao: [");

            float cost = 0;
            for (int i = 0; i < plan.length; i++) {
                if (plan[i] % 2 == 0) {
                    cost += 1.0f;
                }
                else {
                    cost += 1.5f;
                }

                System.out.print(acao[plan[i]]);
                if (i < plan.length-1) {
                    System.out.print(", ");
                }
            }

            System.out.println("]\n" +
                    "Custo total: " + cost + "\n");
        }

        // Imprime o estado do mundo
        System.out.println("=============\n" +
                "ITERACAO " + (ct+1) + "\n");
        model.desenhar();

        int[] ap = prob.acoesPossiveis(estAtu);
        // nao atingiu objetivo e ha acoesPossiveis a serem executadas no plano
        if (!prob.testeObjetivo(estAtu) && ct < plan.length) {
            System.out.println("Estado atual: (" + estAtu.getLin() + "," + estAtu.getCol() + ")");
            System.out.println("Custo parcial: " + custo);
            System.out.print("Acoes possiveis: ");
            for (int i = 0; i < 8; i++) {
                if (ap[i] != -1) {
                    System.out.print(acao[i] + " ");
                }
            }
            System.out.println("\nProxima acao (escolhida): " + acao[plan[ct]] + "\n");

            executarIr(plan[ct]);

            // atualiza custo
            if (plan[ct] % 2 == 0 ) { // acoes pares = N, L, S, O
               custo = custo + 1;
            }
            else {
               custo = custo + 1.5;
            }

            // atualiza estado atual - sabendo que o ambiente eh deterministico
            estAtu = prob.suc(estAtu, plan[ct]);
        }
        else {
            System.out.println("Fim do plano.");
            System.out.println("Estado atual: (" + estAtu.getLin() + "," + estAtu.getCol() + ")");
            System.out.println("Custo: " + custo);
            return -1;
        }
        
        return 1;
    }
    
    /**Funciona como um driver ou um atuador: envia o comando para
     * agente físico ou simulado (no nosso caso, simulado)
     * @param direcao N NE S SE ...
     * @return 1 se ok ou -1 se falha
     */
    public int executarIr(int direcao) {
        model.ir(direcao);
        return 1; 
    }   
    
    // Sensor
    public Estado sensorPosicao() {
        int pos[];
        pos = model.lerPos();
        return new Estado(pos[0], pos[1]);
    }

    // Funcoes que fazem o pathfinding
    // Retornam os arrays com o caminho escolhido

    private int[] gerarPlano() {
        // Cria a raiz da árvore
        TreeNode noInicial = new TreeNode(null);
        ++countNosTotal;
        noInicial.setAction(-1);
        noInicial.setState(prob.estIni);
        noInicial.setGn(0);
        noInicial.setHn(algoritmo == 1 ? custoUniforme(noInicial) : algoritmo == 2 ? AEuclidiano(noInicial) : AChebyshev(noInicial)); // Nem eu to feliz com isso

        // Usando listas/queues em vez de arrays pq eh mais apropriado
        Queue<TreeNode> fronteira = new PriorityQueue<TreeNode>(new fnComparator());
        List<Estado> explorados = new ArrayList<Estado>();

        fronteira.add(noInicial);

        TreeNode noAtual;
        Estado estadoAtual;

        while (true) {
            ++countIterGenPlano;

            if (fronteira.size() == 0) {
                return null; // Falhou a procura
            }

            // Retira o elemento de menor f(n)
            noAtual = fronteira.poll();
            estadoAtual = noAtual.getState();

            // Testa se chegou ao objetivo. Se sim, monta o vetor solução e retorna.
            if(prob.testeObjetivo(estadoAtual)) {
                int[] plan = new int[noAtual.getDepth()];

                TreeNode nodeIterator = noAtual;
                for (int i = noAtual.getDepth() - 1; i >= 0; --i) {
                    plan[i] = nodeIterator.getAction();
                    nodeIterator = nodeIterator.getParent();
                }

                return plan;
            }

            explorados.add(estadoAtual);

            int[] acoes = prob.acoesPossiveis(estadoAtual);
            for (int acao = 0; acao < 8; ++acao) {
                if (acoes[acao] != -1) { // Para cada possivel acao...
                    // Cria um nó na árvore
                    TreeNode filho = noAtual.addChild();
                    ++countNosTotal;
                    filho.setState(prob.suc(estadoAtual, acao));
                    filho.setAction(acao);
                    filho.setGn((noAtual.getGn() + (acao % 2 == 0 ? 1.0f : 1.5f))); // Adiciona custo 1 se for acao N S L O, senao 1.5
                    filho.setHn(algoritmo == 1 ? custoUniforme(filho) : algoritmo == 2 ? AEuclidiano(filho) : AChebyshev(filho)); // Nem eu to feliz com isso

                    TreeNode noAntigo;
                    if (!filho.getState().contidoEm(explorados)) {
                        noAntigo = filho.contidoEm(fronteira);
                        if (noAntigo == null) {
                            // Se o estado não foi explorado, e o nó não está na fronteira, adiciona-o à fronteira.
                            fronteira.add(filho);
                        }
                        else {
                            // Se o estado não foi explorado, e o nó já está na fronteira, compara os custos.
                            ++countNosDescartadosFront;
                            if (noAntigo.getFn() > filho.getFn()) {
                                fronteira.add(filho);
                                fronteira.remove(noAntigo);
                            }
                        }
                    }
                    else {
                        // Se o estado já foi explorado, apenas incrementa o contador
                        ++countNosJaExpl;
                    }
                }
            }
        }
    }

    private float custoUniforme(TreeNode no) {
        return 0.0f;
    }

    private float AEuclidiano(TreeNode no ) {
        return (float) sqrt(
                (prob.estObj.getLin() - no.getState().getLin()) * (prob.estObj.getLin() - no.getState().getLin()) +
                (prob.estObj.getCol() - no.getState().getCol()) * (prob.estObj.getCol() - no.getState().getCol())
        );
    }

    private float AChebyshev(TreeNode no) {
        return (float) max(
                abs(prob.estObj.getLin() - no.getState().getLin()),
                abs(prob.estObj.getCol() - no.getState().getCol())
        );
    }

}
