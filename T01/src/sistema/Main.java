/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistema;

import ambiente.*;

/**
 *
 * @author tacla
 */
public class Main {
    public static void main(String args[]) {
        // Cria o ambiente (modelo) = labirinto 8x8 com suas paredes
        Model model = new Model(8, 8);
        model.porParedeHorizontal(1, 3, 2);
        model.porParedeHorizontal(0, 2, 4);
        model.porParedeHorizontal(4, 7, 4);
        
        // Cria um agente
        Agente ag = new Agente(model);
        
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
