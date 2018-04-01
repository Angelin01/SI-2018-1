
package sistema;

import ambiente.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author tacla
 */
public class Agente {
    /* referência ao ambiente para poder atuar no mesmo*/
    Model model;
    int ct=-1;
           
    public Agente(Model m) {
        this.model = m;       
    }
    
    /**Escolhe qual ação (UMA E SOMENTE UMA) será executada em um ciclo de raciocínio
     * @return 1 enquanto o plano não acabar; -1 quando acabar
     */
    public int deliberar() {
        ct++;

        //executarIr(4); // Escolhido em uma jogada de dados justa
                         // Guarantido ser aleatório
        executarIr(ThreadLocalRandom.current().nextInt(8));
        
        if (ct > 2) 
            return -1;
        
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
    
}
    

