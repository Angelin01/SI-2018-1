/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistema;

import ambiente.*;

import java.util.Scanner;

/**
 *
 * @author tacla
 */
public class Main {
    public static void main(String args[]) {
        // Cria o ambiente (modelo) = labirinto com suas paredes
        Model model = new Model(9, 9);
        model.labir.porParedeVertical(0, 1, 0);
        model.labir.porParedeVertical(0, 0, 1);
        model.labir.porParedeVertical(5, 8, 1);
        model.labir.porParedeVertical(5, 5, 2);
        model.labir.porParedeVertical(8, 8, 2);
        model.labir.porParedeHorizontal(4, 7, 0);
        model.labir.porParedeHorizontal(7, 7, 1);
        model.labir.porParedeHorizontal(3, 5, 2);
        model.labir.porParedeHorizontal(3, 5, 3);
        model.labir.porParedeHorizontal(7, 7, 3);
        model.labir.porParedeVertical(6, 7, 4);
        model.labir.porParedeVertical(5, 6, 5);
        model.labir.porParedeVertical(5, 7, 7);
        
        // seta a posição inicial do agente no ambiente - nao interfere no 
        // raciocinio do agente, somente no amibente simulado
        model.setPos(8, 0);
        model.setObj(2, 8);

        // Para escolher algoritmos
        Scanner in = new Scanner(System.in);
        int algoritmo = 0;

        while(algoritmo < 1 || algoritmo > 3) {
            System.out.println("Escolha um algoritmo:\n" +
                               "1 - Custo Uniforme\n" +
                               "2 - A* com Distancia Euclidiana\n" +
                               "3 - A* com Distancia Chebyshev\n");
            algoritmo = in.nextInt();
        }
        
        // Cria um agente
        Agente ag = new Agente(model, algoritmo);
        
        // Ciclo de execucao do sistema
        // desenha labirinto
        model.desenhar(); 
        
        // agente escolhe proxima açao e a executa no ambiente (modificando
        // o estado do labirinto porque ocupa passa a ocupar nova posicao)
        
        System.out.println("\n*** Inicio do ciclo de raciocinio do agente ***\n");
        while (ag.deliberar() != -1) {  
            model.desenhar(); 
        }
    }
}
