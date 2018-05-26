package comuns;

import java.util.Random;

public class Fruta {
    private char[] cor;

    private static final Random rng = new Random();

    public char[] getCor() {
        return cor;
    }

    public Fruta() {
        cor = new char[5];
        randomize();
    }

    public void randomize() {
        for (int i = 0; i < 5; i++) {
            if (i == 0 || i == 4) {
                if (rng.nextInt(2) == 0) {
                    cor[i] = 'K';
                }
                else {
                    cor[i] = 'W';
                }
            }
            else {
                switch (rng.nextInt(3)) {
                    case 0:
                        cor[i] = 'R';
                        break;
                    case 1:
                        cor[i] = 'G';
                        break;
                    case 2:
                    default:
                        cor[i] = 'B';
                        break;
                }
            }
        }
    }
}
