
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author osmar
 */
public class AFND {

    private JsonNode afndDados;

    private JsonNode entradas;

    private List<String> estadosIniciais = new ArrayList<>();

    private List<String> estadosFinais = new ArrayList<>();

    private List<String> lista = new ArrayList<>();

    private JsonNode matriz;

    private ArrayList<JsonNode> aprovado = new ArrayList<>();

    private ArrayList<JsonNode> rejeitado = new ArrayList<>();

    private String nome_afd;

    /**
     * Construtor sem parâmetro.
     *
     * @param nome_afd
     */
    public AFND(String nome_afd) {
        System.out.println("Inicializando AFND");
        this.nome_afd = nome_afd;
        getDadosJSON();
        setEstadoInicial();
        setEstadoFinal();
        carregaMatriz();
    }

     /**
     * Recupera os dados dos arquivos json.
     */
    private void getDadosJSON() {
        System.out.println("Recuperando informacoes do arquivo \"afnd_" + nome_afd + ".json\"");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            afndDados = objectMapper.readTree(new File("afnd_" + nome_afd + ".json"));
            entradas = objectMapper.readTree(new File("entradas_" + nome_afd + ".json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Define o estado inicial.
     */
    private void setEstadoInicial() {
        System.out.println("Definindo estado inicial");
        afndDados.get("estadoInicial").forEach(
                estado -> {
                    estadosIniciais.add(estado.asText());
                    lista.add(estado.asText());
                }
        );
    }

    /**
     * Define o estado final.
     */
    private void setEstadoFinal() {
        System.out.println("Definindo o estado final");
        afndDados.get("estadoFinal").forEach(
                estado -> estadosFinais.add(estado.asText())
        );
    }

    /**
     * Recupera o alfabeto.
     *
     * @return
     */
    private JsonNode getAlfabeto() {
        System.out.println("Recuperando o alfabeto");
        return afndDados.get("alfabeto");
    }

    /**
     * Recupera a tabela de transição para um estado e entrada.
     *
     * @param nome
     * @param entrada
     * @return
     */
    private JsonNode getTabelaTransicaoEstadoEntrada(String nome, String entrada) {
        //System.out.println("Recuperando tabela de transicao para o estado:" + nome + " e entrada :" + entrada);
        return getMatriz().get(nome).get(entrada);
    }
    
    /**
     * Recupera a matriz de transições de estado.
     *
     * @return
     */
    private JsonNode getMatriz() {
        return matriz;
    }

    /**
     * Carrega a matriz de transições de estado.
     */
    private void carregaMatriz() {
        System.out.println("Recuperando a matriz de transição");
        matriz = afndDados.get("matriz");
    }

    /**
     * Avalia as entradas.
     */
    public void avaliar() {
        System.out.println("\nAvaliando entradas");
        //Percorre as entradas do arquivo entradas_X.json
        for (JsonNode entrada : entradas.get("entradas")) {
            System.out.println("Entrada:" + entrada);

            //Avaliada cada token da entrada
            for (JsonNode token : entrada) {
                //Lista para guardar as transições realizadas
                List<String> listaTransicoes = new ArrayList<>();
                //Para cada estado da matriz
                lista.forEach(estado -> {
                    if (getMatriz().get(estado) != null) {
                        //Recupera as transições do estado para o token de entrada
                        JsonNode transicoes = getTabelaTransicaoEstadoEntrada(estado, token.asText());
                        if (transicoes != null) {
                            //Guarda os estados para a entrada na lista
                            transicoes.forEach(estadoTransicao -> {
                                listaTransicoes.add(estadoTransicao.asText());
                            });
                        }
                    }
                });
                //Analise transições vazias
                List<String> listaTransicoesVazio = new ArrayList<>(listaTransicoes);
                //Percorre os estados internos
                listaTransicoesVazio.forEach(estadoInterno -> {
                    if (getMatriz().get(estadoInterno) != null) {
                        JsonNode transicoesVazia  = getTabelaTransicaoEstadoEntrada(estadoInterno, "ε");
                        if (transicoesVazia != null) {
                            transicoesVazia.forEach(estadoTransicaoVazia -> listaTransicoes.add(estadoTransicaoVazia.asText()));
                        }
                    }
                });
                lista = new ArrayList<>(listaTransicoes);
            }
            //Se o estado final estiver em algum estado da lista de transições
            if (estadosFinais.stream().anyMatch(estado -> lista.contains(estado))) {
                aprovado.add(entrada);
                System.out.println("Aprovada -> " + entrada);
            } else {
                rejeitado.add(entrada);
                System.out.println("Rejeitada -> " + entrada);
            }
            System.out.println("");
        }
        //Salva os resultados
        salvaResultados();
    }

    /**
     * Salva os resultados no arquivo 'resultados_X.json'.
     */
    private void salvaResultados() {
        Map<String, List<JsonNode>> resultados = new HashMap<>();
        resultados.put("Aprovado", aprovado);
        resultados.put("Rejeitado", rejeitado);
        System.out.println("Salvando o resultado em \"resultados_" + nome_afd + ".json\"");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("resultados_" + nome_afd + ".json"), resultados);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}