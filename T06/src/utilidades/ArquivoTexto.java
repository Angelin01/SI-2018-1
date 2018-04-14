package utilidades;

import java.io.FileWriter;
import java.io.IOException;

/**Operações sobre um arquivo texto de saída que serve para analise de 
 * desempenho dovalgoritmo.
 * @author tacla
 */
public class ArquivoTexto {

    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    public String fileName = "C:/arq.txt";
    private FileWriter fileWriter;
    
    public ArquivoTexto(String name) {
        this.fileName = name;
        createFile();
    }

    // em arquivos CSV, cabecalho
    public void appendTextWithNewLine(String s) {
        try {
            fileWriter.append(s);
            fileWriter.append(NEW_LINE_SEPARATOR);
        } catch (IOException e) {
            System.out.printf("ERRO AO ADICIONAR TEXTO AO ARQUIVO: %s\n%s", fileName, e.getMessage());
        }
    }
        public void appendText(String s) {
        try {
            fileWriter.append(s);
        } catch (IOException e) {
            System.out.printf("ERRO AO ADICIONAR TEXTO AO ARQUIVO: %s\n%s", fileName, e.getMessage());
        }
    }

    private void createFile() {
        try {
            // 
            fileWriter = new FileWriter(fileName, true);
        } catch (IOException e) {
            System.out.printf("ERRO AO ABRIR/CRIAR ARQUIVO: %s\n%s", fileName, e.getMessage());
        }
    }

    public void closeFile() {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            System.out.printf("ERRO AO FECHAR ARQUIVO: %s\n%s", fileName, e.getMessage());
        }
    }
}
