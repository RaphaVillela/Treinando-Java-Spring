package br.com.site.screenmatch.main;

import br.com.site.screenmatch.model.*;
import br.com.site.screenmatch.repository.SerieRepository;
import br.com.site.screenmatch.service.ConsumoApi;
import br.com.site.screenmatch.service.ConverteDados;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    Dotenv dotenv = Dotenv.load();

    private Scanner leitura = new Scanner(System.in);

    private ConsumoApi consumoApi = new ConsumoApi();

    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";

    private final String API_KEY = "&apikey=" + dotenv.get(("OMDB_KEY"));

    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository repositorio;

    private List<Serie> series = new ArrayList<>();

    private Optional<Serie> serieBusca;

    public Main(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {

        var menu = """
                Digite o numero correspondente:
                1 - Buscar séries
                2 - Buscar episódios das séries
                3 - Verificar Séries adicionadas
                4 - Buscar Séries pelo nome
                5 - Buscar Séries por ator
                6 - Top 5 Séries
                7 - Buscar por Categoria
                8 - Filtrar Séries
                9 - Buscar Episodio por Trecho
                10 - Melhores 5 Episódios da Série
                11 - Buscar Episodios a partir de uma data
                
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
                case 4:
                    buscaSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    filtrarSeries();
                    break;
                case 9:
                    buscaEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosAposData();
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
        DadosSerie dadosSerie = getDadosSerie();
        Serie serie = new Serie(dadosSerie);
        repositorio.save(serie);
        System.out.println(dadosSerie);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite a serie que deseja buscar:");
        var nomeSerie = leitura.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscaEpisodioSerieAPI() {
        //DadosSerie serie = getDadosSerie();
        mostrarSeriesBuscadas();

        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            List<DadosTemporada> temporadas = new ArrayList<>();

            var serieEncontrada = serie.get();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {

                var json = consumoApi.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else  {
            System.out.println("Série não encontrada!");
        }
    }

    private void mostrarSeriesBuscadas() {
        series = repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getCategoria))
                .forEach(System.out::println);
    }

    private void buscaSeriePorTitulo() {
        System.out.println("Digite a serie que deseja buscar:");
        var nomeSerie = leitura.nextLine();

        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da série: " + serieBusca.get());
        } else {
            System.out.println("Série não encontrada");
        }
    }

    private void buscarSeriePorAtor() {
        System.out.println("Digite o ator que deseja buscar:");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliações a partir de que valor?");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);

        System.out.println("Séries em que " + nomeAtor + " aparece: ");
        seriesEncontradas.forEach(s ->
                System.out.println("Titulo: " + s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
    }

    private void buscarTop5Series() {
        List<Serie> top5Series = repositorio.findTop5ByOrderByAvaliacaoDesc();
        top5Series.forEach(s ->
                System.out.println("Titulo: " + s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {

        System.out.println("Digite a categoria da Série que deseja buscar:");
        var genero = leitura.nextLine();

        Categoria categoria = Categoria.fromPortugues(genero);

        List<Serie> seriesPorCategoria = repositorio.findByCategoria(categoria);

        System.out.println("Séries da Categoria: " + genero);
        seriesPorCategoria.forEach(s ->
                System.out.println("Titulo: " + s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
    }

    private void filtrarSeries() {
        System.out.println("Qual o número máximo de temporadas? ");
        Integer temporadas = leitura.nextInt();
        leitura.nextLine();

        System.out.println("Qual é a nota mínima da série?");
        Double nota = leitura.nextDouble();

        List<Serie> filtrados = repositorio.seriesPorTemporadaEAvaliacao(temporadas, nota);

        filtrados.forEach(s ->
                System.out.println("Titulo: " + s.getTitulo() + " Avaliação: " + s.getAvaliacao() + " Temporadas: " + s.getTotalTemporadas()));
    }

    private void buscaEpisodioPorTrecho() {
        System.out.println("Insira o nome do episódio para buscar");
        String trecho = leitura.nextLine();

        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trecho);

        episodiosEncontrados.forEach(e ->
                System.out.printf("Série: %s  Temporada: %s - Episódio: %s - %s\n",
                e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodios(), e.getTitulo()));
    }

    private void topEpisodiosPorSerie() {
        buscaSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);

            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s  Temporada: %s - Episódio: %s - %s Avaliação: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodios(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscarEpisodiosAposData() {
        buscaSeriePorTitulo();;
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite de lançamento:");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodiosAno = repositorio.episodioPorSerieEAno(serie, anoLancamento);

            episodiosAno.forEach(System.out::println);        }
    }
}
