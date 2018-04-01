package ambiente;

/**Desenha o ambiente (o que está representado no Model) em
 * formato texto.
 *
 * @author tacla 
 */
class View {
    private Model model;
    protected View(Model m) {
        this.model = m;
    }

    protected void desenhar() {
        System.out.println("--- Estado do AMBIENTE ---");
        System.out.println(model.pos[0] + "," + model.pos[1]);
        for (int lin = 0; lin < model.maxLin; lin++) {
            for (int col = 0; col < model.maxCol; col++) {
                System.out.print("+---");
            }
            System.out.print("+\n");
            for (int col = 0; col < model.maxCol; col++) {
                if (model.lab.parede[lin][col] == 1) {
                    System.out.print("|XXX");  // desenha parede
                } else if (model.pos[0] == lin && model.pos[1] == col) {
                    System.out.print("| A ");  // desenha agente
                } else {
                    System.out.print("|   ");  // posicao vazia
                }
            }
            System.out.print("|");
            if (lin == (model.maxLin - 1)) {
                System.out.print("\n");
                for (int x = 0; x < model.maxCol; x++) {
                    System.out.print("+---");
                }
                System.out.println("+\n");
            }
            System.out.print("\n");
        }
    }
}
