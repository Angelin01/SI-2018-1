/** Representa um estado do problema. Neste caso, é um par ordenado que representa
 * a linha e a coluna onde se encontra o agente no labirinto.
 * @author Tacla UTFPR
 * @version 2017-08
 */
package problema;

import java.util.List;

public class Estado {
    /** linha onde se encontra o agente */
    private int lin;
    /** coluna onde se encontra o agente */
    private int col;
    
    public Estado(int lin, int col) {
        this.lin = lin;
        this.col = col;
    }

    public void setLinCol(int lin, int col) {
        this.lin = lin;
        this.col = col;
    }
    
    public boolean igualAo(Estado a) {
        return this.lin == a.lin && this.col == a.col;
    }

    // Retorna true se o estado estiver na lista. Em caso contrário, retorna false.
    public boolean contidoEm(List<Estado> al) {
        boolean contido = false;

        for (Estado e : al) {
            if (this.igualAo(e)) {
                contido = true;
                break;
            }
        }

        return contido;
    }

    public int getLin() {
        return this.lin;
    }

    public int getCol() {
        return this.col;
    }
    public String getString() {
        String str;
        str = String.format("(%d, %d)", lin, col);
        return str;        
    }

    
}
