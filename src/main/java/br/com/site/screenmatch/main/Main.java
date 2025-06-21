package br.com.site.screenmatch.main;

import br.com.site.screenmatch.model.*;
import br.com.site.screenmatch.service.ConsumoApi;
import br.com.site.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private Scanner leitura = new Scanner(System.in);

    private ConsumoApi consumoApi = new ConsumoApi();

    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";

    private final String API_KEY = "&apikey=902024ae";

    private List<DadosSerie> dadosSeries = new ArrayList<>();

    public void exibeMenu() {

        var menu = """
                Digite o numero correspondente:
                1 - Buscar séries
                2 - Buscar episódios das séries
                3 - Verificar Séries adicionadas
                
                0 - Sair
                """;

        var opcao = -1;

        while (opcao != 0) {
            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscaSerieAPI();
                    break;
                case 2:
                    buscaEpisodioSerieAPI();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 0:
                    System.out.println("Encerrando programa...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscaSerieAPI() {
        DadosSerie serie = getDadosSerie();
        dadosSeries.add(serie);
        System.out.println(serie);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite a serie que deseja buscar:");
        var nomeSerie = leitura.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscaEpisodioSerieAPI() {
        DadosSerie serie = getDadosSerie();

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= serie.totalTemporadas(); i++) {

            var json = consumoApi.obterDados(ENDERECO + serie.titulo().replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);
    }

    private void mostrarSeriesBuscadas() {
        List<Serie> series = new ArrayList<>();
        series = dadosSeries.stream().map(d -> new Serie(d))
                        .collect(Collectors.toList());

        series.stream()
                .sorted(Comparator.comparing(Serie::getCategoria))
                .forEach(System.out::println);
    }

}
