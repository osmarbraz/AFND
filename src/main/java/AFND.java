
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

    private List<String> estadoInicial = new ArrayList<>();

    private List<String> estadoFinal = new ArrayList<>();

    private List<String> lista = new ArrayList<>();

    private JsonNode matriz;

    private ArrayList<JsonNode> aprovado = new ArrayList<>();

    private ArrayList<JsonNode> rejeitado = new ArrayList<>();

    private Map<String, List<JsonNode>> resultados = new HashMap<>();

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
        setTabelaTransicaoEstados();
    }

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
                    estadoInicial.add(estado.asText());
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
                estado -> estadoFinal.add(estado.asText())
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
        //System.out.println("Recuperando matriz transicao para o estado e entrada");
        return matriz.get(nome).get(entrada);
    }

    /**
     * Carrega os transições de estado.
     */
    private void setTabelaTransicaoEstados() {
        System.out.println("Recuperando a matriz de transicao");
        matriz = afndDados.get("matriz");
    }

    /**
     * Avalia as entradas.
     */
    public void avaliar() {
        System.out.println("Avaliando entradas");
        for (JsonNode listaEntrada : entradas.get("entradas")) {
            System.out.println("Entrada:" + listaEntrada);

            //Avaliada cada caracter da entrada
            for (JsonNode entrada : listaEntrada) {
                List<String> listaTransicoes = new ArrayList<>();
                lista.forEach(estado -> {
                    if (matriz.get(estado) != null) {
                         JsonNode transicoes = getTabelaTransicaoEstadoEntrada(estado, entrada.asText());
                        if (transicoes != null) {
                            transicoes.forEach(estadoTransicao -> {
                                listaTransicoes.add(estadoTransicao.asText());
                            });
                        }
                    }
                });

                List<String> listaTransicoesVazio = new ArrayList<>(listaTransicoes);
                listaTransicoesVazio.forEach(estadoInterno -> {
                    if (matriz.get(estadoInterno) != null) {
                        JsonNode transicoesVazia  = getTabelaTransicaoEstadoEntrada(estadoInterno, "ε");
                        if (transicoesVazia != null) {
                            transicoesVazia.forEach(estadoTransicaoVazia -> listaTransicoes.add(estadoTransicaoVazia.asText()));
                        }
                    }
                });
                lista = new ArrayList<>(listaTransicoes);
            }
            if (estadoFinal.stream().anyMatch(estado -> lista.contains(estado))) {
                aprovado.add(listaEntrada);
                System.out.println("Aprovada -> " + listaEntrada);
            } else {
                rejeitado.add(listaEntrada);
                System.out.println("Rejeitada -> " + listaEntrada);
            }
            System.out.println("");
        }
        salvaResultados();
    }

    /**
     * Salva os resultados no arquivo 'resultados.json'.
     */
    private void salvaResultados() {
        resultados.put("Aprovado", aprovado);
        resultados.put("Rejeitado", rejeitado);
        System.out.println("Salvando o resultado em \"resultados_" + nome_afd + ".json\"");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("resultados_" + nome_afd + ".json"), this.resultados);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
