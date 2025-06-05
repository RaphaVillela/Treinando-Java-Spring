package br.com.site.screenmatch;

import br.com.site.screenmatch.model.DadosSerie;
import br.com.site.screenmatch.service.ConsumoApi;
import br.com.site.screenmatch.service.ConverteDados;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		var consumoApi = new ConsumoApi();
		//String busca = game of thrones&Season=1;
		//String endereco = "https://www.omdbapi.com/?t=" + busca.replace(" ", "+") + "&apikey=902024ae";
		//var url = "https://api.adviceslip.com/advice";
		var url = "https://www.omdbapi.com/?t=game+of+thrones&Season=1&apikey=902024ae";
		var json = consumoApi.obterDados(url);
		System.out.println(json);
		ConverteDados conversor = new ConverteDados();
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		System.out.println(dados);
	}
}
