/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package subidaencosta;

import utilidades.ArquivoTexto;

/**Implementacao do algoritmo de busca local Hill Climbing para um quebra-
 * cabeças simples (satisfação de restrições).
 * @author tacla
 */
public class SubidaEncosta {
    /** numero sequencial que identifica a execucao atual */
    protected static int seqExecucao = 0; 
    /** qtd de estados calculados para cada estado atual */
    protected static int K_VIZINHOS; 
    //** critério de parada: número máximo de iterações */
    protected static int K_ITERACOES; 
    /** armazena o estado atual na pos 0 e os K sucessores nas posicoes subsequentes */
    Tabuleiro estAtu; 
    /** Cada linha deste arq contém os parâmetros de execução e o resultado da execução */
    protected static ArquivoTexto arqTodasExecs; 
    /** Cada linha deste arq apresenta somente o melhor estado por iteração */
    protected static ArquivoTexto arqMelhorEstIter;  

    /** Um objeto SubidaEncosta para cada execução
     * @param vizinhos número de vizinhos a serem calculados por estado
     * @param iteracoes número máximo de iterações (condição de parada)
     */
    public SubidaEncosta(int vizinhos, int iteracoes) {
        K_VIZINHOS = vizinhos;
        K_ITERACOES = iteracoes;
        seqExecucao++;
    }
    /** Realiza a busca Subida de Encosta
     * 
     * @return boolean true quando a busca encontra uma solução
     *                 false quando encerram-se o número previstos de iterações
     *                       e a solução não foi encontrada
     */
    public boolean buscar() {
        int ctAvaliacoes = 0; // conta quantas vezes h foi calculada (1 x por vizinho gerado) 
        
        // cria o tabuleiro no estado inicial: com pedras em posicoes aleatorias 
        estAtu = new Tabuleiro();
        estAtu.inicializarTabuleiroAleatoriamente();
        ctAvaliacoes++;

        int iter = 0;
        int ctViz; // conta vizinhos gerados
        boolean estagnou = false; // criterio de parada

        // Encerra a busca qdo nao consegue melhorar estado atual, qdo encontrar a solucao
        while (!estagnou && estAtu.vlrObj > 0 && iter < K_ITERACOES) {
            // gera K Vizinhos
            ctViz = 0;
            // armazena o melhor vizinho do estado atual
            Tabuleiro melhorViz = null;         
            // armazena o valor da função objetivo para o melhor vizinho
            int melhorVlrObj = Integer.MAX_VALUE; 
            while (ctViz < K_VIZINHOS) {
                // Cria um estado vizinho
                Tabuleiro viz = new Tabuleiro();
                viz.gerarVizinho(estAtu); 
                ctAvaliacoes++;
                ctViz++;

                // salva o melhor vizinho entre os K gerados
                if (viz.vlrObj < melhorVlrObj) {
                    melhorViz = viz;
                    melhorVlrObj = viz.vlrObj;
                    if (viz.vlrObj < 1)           // teste de objetivo
                    {
                        break;
                    }
                }
            }
            // Compara melhor vizinho com o estado atual: 
            // como eh MINIMIZACAO escolhe o menor valor da função objetivo
            if (melhorViz != null) {
                if (melhorVlrObj < estAtu.vlrObj) { 
                    // melhor vizinho se torna o atual
                    estAtu = melhorViz;
                } else {    
                    // algoritmo estagnou em um minimo local
                    estagnou = true;
                }
            }
            iter++;
            
            // grava parametros da execucao no arquivo texto 
            arqMelhorEstIter.appendTextWithNewLine(seqExecucao + "," + K_VIZINHOS + ","
                    + K_ITERACOES + "," + iter + "," + estAtu.imprimirTabuleiroCSV());
        }
        arqTodasExecs.appendText(String.valueOf(seqExecucao) + ","
                + String.valueOf(K_VIZINHOS) + ","
                + String.valueOf(K_ITERACOES) + ","
                + String.valueOf(iter) + ","
                + String.valueOf(ctAvaliacoes) + ",");

        arqTodasExecs.appendTextWithNewLine(estAtu.imprimirTabuleiroCSV());

        if (estAtu.vlrObj == 0) {
            System.out.println("**************************************");
            System.out.println("**        SOLUCAO ENCONTRADA        **");
            System.out.println("**************************************");
            estAtu.imprimirTabuleiro();
            return true;
        }
        return false;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int execucoes = 300;
        // Cria ou abre arquivos para guardar resultados
        // Arquivo com o resultado de cada execucao
        arqTodasExecs = new ArquivoTexto("SE_Resultados.csv");

        // cabecalho do arquivo: soh armazena o resultado final
        arqTodasExecs.appendTextWithNewLine("exec,K-viz,K-iter,iter-fim,aval,vlrObj");

        // Arquivo com os melhores estados por iteracao
        arqMelhorEstIter = new ArquivoTexto("SE_MelhoresPorIter.csv");

        // cabecalho do arquivo Melhores Estados por iteracao
        arqMelhorEstIter.appendTextWithNewLine("exec,K-viz,K-iter,iter,vlrObj");

        for (int i = 0; i < execucoes; i++) {
            System.out.println("************* EXECUCAO " + i + "********************");
            SubidaEncosta se = new SubidaEncosta(5, 50); // qtd est. sucessores e iteracoes
            // interrompe execuções se encontrar a solução
            if (se.buscar()) {
                break;
            }
        }
        arqTodasExecs.closeFile();
        arqMelhorEstIter.closeFile();
    }
}
