/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistema;

import ambiente.*;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author tacla
 */
public class Main {
    public static void main(String args[]) throws IOException {
        // Cria o ambiente (modelo) = labirinto com suas paredes
        Model model = new Model(5, 9);
        model.labir.porParedeHorizontal(7, 8, 0);
        model.labir.porParedeHorizontal(0, 1, 1);
        model.labir.porParedeHorizontal(3, 5, 1);
        model.labir.porParedeHorizontal(3, 6, 2);
        model.labir.porParedeHorizontal(3, 5, 3);
        model.labir.porParedeVertical(3, 4, 1);
        
        // seta a posição inicial do agente no ambiente - nao interfere no 
        // raciocinio do agente, somente no amibente simulado
        model.setPos(4, 0);
        model.setObj(2, 8);

        // Para escolher algoritmos
        Scanner in = new Scanner(System.in);
        int heuristica = 0;

        while(heuristica < 1 || heuristica > 3) {
            System.out.println("Escolha uma heurística:\n" +
                               "1 - Zero\n" +
                               "2 - Distancia Euclidiana\n" +
                               "3 - Distancia Chebyshev\n");
            heuristica = in.nextInt();
        }
        in.nextLine(); // Que xunxo horrivel odeio Java
        
        // Cria um agente
        Agente ag = new Agente(model, heuristica);
        
        // Ciclo de execucao do sistema
        // desenha labirinto
        model.desenhar();

        // agente escolhe proxima açao e a executa no ambiente (modificando
        // o estado do labirinto porque ocupa passa a ocupar nova posicao)
        int itCounter = 0;
        String input;
        
        System.out.println("\n*** Inicio do ciclo de raciocinio do agente ***\n");
        do {
            System.out.println("\n\n========================\nNova iteração\n========================\n");
            // Reseta a posição do agente, mas não altera a matriz h(n)
            model.setPos(4, 0);
            ag.reset();

            while (ag.deliberar() != -1); // O agente imprime o estado do mundo, conforme solicitado no enunciado do T04.

            System.out.println("\nCusto final: " + ag.getCusto() +
                               "\nRazao de competitividade: " + ag.getCusto() + "/11.5" + " = " + ag.getCusto()/11.5f);
            System.out.println("\n\n========================\nIteração " + ++itCounter + " Completa\n========================\n" +
                               "Digite 'q' para terminar ou qualquer outra coisa para continuar");
            input = in.next(); // Java eh um lixo
            in.nextLine();
        } while(!input.equals("q"));
    }
}
