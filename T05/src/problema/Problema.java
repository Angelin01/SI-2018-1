/** Representação de um problema a ser resolvido por um algoritmo de busca clássica.
 * A formulação do problema - instância desta classe - reside na 'mente' do agente.
 *
 * @author Tacla
 */
package problema;

import comuns.*;

/**
 *
 * @author tacla
 */
public class Problema implements PontosCardeais {

    /**Estado inicial para um problema de busca (linha, coluna) */
    public Estado estIni = new Estado(0, 0);
    /**Estado objetivo para um problema de busca (linha, coluna) */
    public Estado estObj = new Estado(0, 0);
    /**Crença do agente sobre o estado do labirinto, i.e. a posição de suas paredes */
    public Labirinto crencaLabir;


    /**Este método instancia um labirinto - representa o que o agente crê ser
     * o labirinto. As paredes devem ser colocadas fora desta classe porque este
     * pacote é utilizado para construir labirintos diferentes.
     * @param maxLin máximo de linhas do labirinto
     * @param maxCol máximo de colunas do labirinto
     */
    public void criarLabirinto(int maxLin, int maxCol) {
        this.crencaLabir = new Labirinto(maxLin, maxCol);
       
    }

    /**
     * Define estado inicial
     * @param lin
     * @param col
     */
    public void defEstIni(int lin, int col) {
        estIni.setLinCol(lin, col);
    }

    /**
     * Define estado objetivo
     * @param lin
     * @param col
     */
    public void defEstObj(int lin, int col) {
        estObj.setLinCol(lin, col);
    }

    /**
     * Funcao sucessora: recebe um estado '(lin, col)' e calcula o estado
     * sucessor que resulta da execucao da acao = {N, NE, L, SE, S, SO, O, NO}
     * @param est
     * @param acao
     * @return 
     */
    public Estado suc(Estado est, int acao) {
        int lin = est.getLin();
        int col = est.getCol();

        switch (acao) {
            case N:
                lin--;
                break;
            case NE:
                col++;
                lin--;
                break;
            case L:
                col++;
                break;
            case SE:
                col++;
                lin++;
                break;
            case S:
                lin++;
                break;
            case SO:
                col--;
                lin++;
                break;
            case O:
                col--;
                break;
            case NO:
                col--;
                lin--;
                break;
        }
        // verifica se está fora do grid
        if (col < 0 || col >= crencaLabir.getMaxCol() || lin < 0 || lin >= crencaLabir.getMaxLin()) {
            lin = est.getLin();
            col = est.getCol();  // fica na posicao atual
        }
        // verifica se bateu em algum obstaculo
        if (crencaLabir.parede[lin][col] == 1) {
            lin = est.getLin();
            col = est.getCol();  // fica na posicao atual
        }

        // retorna estado sucessor
        return new Estado(lin, col);
    }

    /**
     * Retorna as acoesPossiveis possiveis de serem executadas em um estado 
     * O valor retornado é um vetor de inteiros. Se o valor da posicao é -1
     * então a ação correspondente não pode ser executada, caso contrario valera 1.
     * Por exemplo, 
     * [-1, -1, -1, 1, 1, -1, -1, -1] indica apenas que S e SO podem ser executadas.
     * @param est
     * @return 
     */
    public int[] acoesPossiveis(Estado est) {
        int acoes[] = new int[8];

        // testa se pode ir para o N, NE ou NO sem sair do limite do labirinto
        if (est.getLin() == 0) {
            acoes[0] = acoes[1] = acoes[7] = -1;
        }
        // testa se pode ir para o NE, L ou SE sem sair do limite do labirinto
        if (est.getCol() == (crencaLabir.getMaxCol() - 1)) {
            acoes[1] = acoes[2] = acoes[3] = -1;
        }
        // testa se pode ir para o SE, S ou SO sem sair do limite do labirinto
        if (est.getLin() == (crencaLabir.getMaxLin() - 1)) {
            acoes[3] = acoes[4] = acoes[5] = -1;
        }
        // testa se pode ir para o SO, O ou NO sem sair do limite do labirinto
        if (est.getCol() == 0) {
            acoes[5] = acoes[6] = acoes[7] = -1;
        }
        // testa se ha paredes no entorno l, c
        int l = est.getLin();
        int c = est.getCol();

        // testa se ha parede na direcao N
        if (acoes[0] != -1 && crencaLabir.parede[l - 1][c] == 1) {
            acoes[0] = -1;
        }

        // testa se ha parede na direcao NE
        if (acoes[1] != -1 && crencaLabir.parede[l - 1][c + 1] == 1) {
            acoes[1] = -1;
        }

        // testa se ha parede na direcao l
        if (acoes[2] != -1 && crencaLabir.parede[l][c + 1] == 1) {
            acoes[2] = -1;
        }

        // testa se ha parede na direcao SE
        if (acoes[3] != -1 && crencaLabir.parede[l + 1][c + 1] == 1) {
            acoes[3] = -1;
        }

        // testa se ha parede na direcao S
        if (acoes[4] != -1 && crencaLabir.parede[l + 1][c] == 1) {
            acoes[4] = -1;
        }

        // testa se ha parede na direcao SO
        if (acoes[5] != -1 && crencaLabir.parede[l + 1][c - 1] == 1) {
            acoes[5] = -1;
        }

        // testa se ha parede na direcao O
        if (acoes[6] != -1 && crencaLabir.parede[l][c - 1] == 1) {
            acoes[6] = -1;
        }

        // testa se ha parede na direcao NO
        if (acoes[7] != -1 && crencaLabir.parede[l - 1][c - 1] == 1) {
            acoes[7] = -1;
        }
        return acoes;
    }

    /**
     * Retorna o custo da acao para ir do estado 1 ao estado 2
     * @param est1
     * @param acao
     * @param est2
     * @return 
     */
    public float obterCustoAcao(Estado est1, int acao, Estado est2) {
        if (acao == N || acao == L || acao == O || acao == S) {
            return (float) 1;
        } else {
            return (float) 1.5;
        }
    }

    /**
     * Retorna true quando estado atual = estado objetivo, caso contrario retorna falso
     * @param estAtu estado atual
     * @return true se o estado atual for igual ao estado objetivo
     */
    public boolean testeObjetivo(Estado estAtu) {
        return this.estObj.igualAo(estAtu);
    }
}
