/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package subidaencosta;

import java.util.Random;

/**
 *
 * @author tacla
 */
public class Sorteio {

    private final Random gerador = new Random();

    // sorteia um numero no intervalo [inf, sup]
    public int sortearItem(int inf, int sup) {
        int n = sup - inf + 1;
        if (n > 0) {
            return gerador.nextInt(n) + inf;
        } else {  // range not representable as int
            return Math.abs(inf);
        }
    }
}
