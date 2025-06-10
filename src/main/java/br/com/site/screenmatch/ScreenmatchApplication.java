package br.com.site.screenmatch;

import br.com.site.screenmatch.main.Main;
import br.com.site.screenmatch.model.DadosEpisodio;
import br.com.site.screenmatch.model.DadosSerie;
import br.com.site.screenmatch.model.DadosTemporada;
import br.com.site.screenmatch.service.ConsumoApi;
import br.com.site.screenmatch.service.ConverteDados;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {

		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		Main main = new Main();
		main.exibeMenu();

	}
}
