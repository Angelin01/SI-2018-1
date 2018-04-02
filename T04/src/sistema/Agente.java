package sistema;

import ambiente.Model;
import problema.Estado;
import problema.Problema;
import comuns.Labirinto;
import comuns.PontosCardeais;
import arvore.TreeNode;
import arvore.fnComparator;

import java.util.ArrayList;
import java.util.List;

import static comuns.PontosCardeais.*;

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
        if(++ct == 0) {
            // Nao foi colocado nenhum check de falha, desnecessario por enquanto
            plan = gerarPlano();
        }

        int ap[];
        ap = prob.acoesPossiveis(estAtu);
        // nao atingiu objetivo e ha acoesPossiveis a serem executadas no plano
        if (!prob.testeObjetivo(estAtu) && ct < plan.length) {
           System.out.println("estado atual: " + estAtu.getLin() + "," + estAtu.getCol());
           System.out.print("açoes possiveis: {");
           for (int i=0;i<ap.length;i++) {
               if (ap[i]!=-1)
                   System.out.print(acao[i]+" ");
           }


           executarIr(plan[ct]);
           
           // atualiza custo
           if (plan[ct] % 2 == 0 ) // acoes pares = N, L, S, O
               custo = custo + 1;
           else
               custo = custo + 1.5;
           
           System.out.println("}\nct = "+ ct + " de " + (plan.length-1) + " ação escolhida=" + acao[plan[ct]]);
           System.out.println("custo ate o momento: " + custo);
           System.out.println("**************************\n\n");
           
           // atualiza estado atual - sabendo que o ambiente eh deterministico
           estAtu = prob.suc(estAtu, plan[ct]);
                      
        }
        else
            return (-1);
        
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
        fnComparator comparador = new fnComparator();

        TreeNode noInicial = new TreeNode(null);
        noInicial.setAction(-1);
        noInicial.setState(prob.estIni);
        noInicial.setGn(0);
        noInicial.setHn(algoritmo == 1 ? custoUniforme(noInicial) : algoritmo == 2 ? AEuclidiano(noInicial) : AChebyshev(noInicial)); // Nem eu to feliz com isso

        // Usando listas em vez de arrays pq eh mais apropriado
        List<TreeNode> fronteira = new ArrayList<TreeNode>();
        fronteira.add(noInicial);

        List<TreeNode> explorados = new ArrayList<TreeNode>();

        TreeNode noAtual;
        Estado proxEstado;

        // Codigo Burro
        boolean inFronteira;
        boolean inExplorados;
        int indexGNMaior;

        while(true) {
            if (fronteira.size() == 0) {
                return (null); // Falhou a procura
            }

            fronteira.sort(comparador);
            noAtual = fronteira.remove(0);

            if(prob.testeObjetivo(noAtual.getState())) {
                List<Integer> solucao = new ArrayList<Integer>();
                TreeNode reconstrutor = noAtual;
                while(reconstrutor.getParent() != null) {
                    solucao.add(reconstrutor.getAction());
                    reconstrutor = reconstrutor.getParent();
                }

                int plan[] = solucao.stream().mapToInt(i->i).toArray();
                int aux;
                for(int i = 0; i < plan.length / 2; ++i) { // Plano eh adicionado ao contrario, inverter
                    aux = plan[i];
                    plan[i] = plan[plan.length - i - 1];
                    plan[plan.length - i - 1] = aux;
                }

                return(plan); // NEM SEI MAIS, ver https://stackoverflow.com/a/23945015
            }

            explorados.add(noAtual);
            for(int acao = 0; acao < 8; ++acao) { // Para cada possivel acao...
                // Codigo burro
                inFronteira = false;
                inExplorados = false;
                indexGNMaior = -1;

                proxEstado = prob.suc(noAtual.getState(), acao);
                if(!proxEstado.igualAo(noAtual.getState())) { // Apenas adicionar se eh uma movimentacao valida
                    TreeNode filho = new TreeNode(noAtual);
                    filho.setState(proxEstado);
                    filho.setAction(acao);
                    filho.setGn((float) (noAtual.getGn() + (acao % 2 == 0 ? 1 : 1.5))); // Adiciona custo 1 se for acao N S L O, senao 1.5
                    filho.setHn(algoritmo == 1 ? custoUniforme(filho) : algoritmo == 2 ? AEuclidiano(filho) : AChebyshev(filho)); // Nem eu to feliz com isso

                    // Codigo burro
                    for(TreeNode no: fronteira) {
                        if(filho.getState().igualAo(no.getState())) {
                            inFronteira = true;
                            if(filho.getGn() < no.getGn()) {
                                indexGNMaior = fronteira.indexOf(no);
                            }
                        }
                    }
                    for(TreeNode no: explorados) {
                        if(filho.getState().igualAo(no.getState())) {
                            inExplorados = true;
                        }
                    }

                    if(!inFronteira && !inExplorados) {
                        fronteira.add(filho);
                    }
                    else if(inFronteira && indexGNMaior >= 0) {
                        fronteira.add(filho);
                        fronteira.remove(indexGNMaior);
                    }
                }
            }
        }
    }

    private float custoUniforme(TreeNode no) {
        return((float)0);
    }

    private float AEuclidiano(TreeNode no ) {
        //@todo
        return(0);
    }

    private float AChebyshev(TreeNode no) {
        //@todo
        return(0);
    }
}
    

