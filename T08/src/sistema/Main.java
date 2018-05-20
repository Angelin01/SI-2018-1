/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistema;

import ambiente.*;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static comuns.PontosCardeais.acao;

/**
 *
 * @author tacla
 */
public class Main {
    private static int PARAMETRO_ESTAGNACAO = 100;

    public static void main(String args[]) throws IOException {
        // Cria o ambiente (modelo) = labirinto com suas paredes
        Model model = new Model(9, 9);

        model.labir.porParedeHorizontal(1, 2, 8);
        model.labir.porParedeVertical(6, 7, 1);
        model.labir.porParedeHorizontal(2, 2, 5);
        model.labir.porParedeVertical(6, 7, 4);
        model.labir.porParedeVertical(5, 6, 5);
        model.labir.porParedeVertical(5, 7, 7);
        model.labir.porParedeHorizontal(3, 6, 3);
        model.labir.porParedeHorizontal(3, 5, 2);
        model.labir.porParedeHorizontal(0, 0, 1);
        model.labir.porParedeHorizontal(0, 1, 0);
        model.labir.porParedeHorizontal(4, 7, 0);

        // seta a posição inicial do agente no ambiente - nao interfere no
        // raciocinio do agente, somente no amibente simulado
        model.setPos(8, 0);
        model.setObj(2, 6);


        // Para escolher algoritmos
        Scanner in = new Scanner(System.in);
        int heuristica = 2;
        
        // Cria um agente
        Agente ag = new Agente(model, heuristica);
        
        // Ciclo de execucao do sistema
        // desenha labirinto
        model.desenhar();

        // agente escolhe proxima açao e a executa no ambiente (modificando
        // o estado do labirinto porque ocupa passa a ocupar nova posicao)
        int itCounter = 0;
        double custoUltimaSolucao = 0;
        int estagnacao = 0;
        
        System.out.println("Iniciando LRTA*: ");
        System.out.println("Estagnação atingida quando estabilizar por " + PARAMETRO_ESTAGNACAO + " iterações");
        System.out.println();

        do {
            // Reseta a posição do agente, mas não altera a matriz h(n)
            model.setPos(8, 0);
            ag.reset();

            while (ag.deliberar() != -1); // O agente imprime o estado do mundo, conforme solicitado no enunciado do T04.

            ++itCounter;

            if (custoUltimaSolucao != ag.getCusto()) {
                estagnacao = 0;
                custoUltimaSolucao = ag.getCusto();
            }
            else {
                estagnacao++;
            }
        } while(estagnacao != PARAMETRO_ESTAGNACAO);

        System.out.println();
        System.out.println("LRTA* finalizado: ");
        System.out.println("Total de iterações: " + itCounter);
        System.out.println("Soluções ótimas encontradas: ");

        for (List<Integer> list : ag.solucoesOtimas) {
            for (int a : list) {
                System.out.print(acao[a] + " ");
            }
            System.out.println();
        }

        System.out.println();

    }
}
