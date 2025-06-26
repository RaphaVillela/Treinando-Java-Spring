package br.com.site.screenmatch.repository;

import br.com.site.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieRepository extends JpaRepository<Serie, Long > {
}
