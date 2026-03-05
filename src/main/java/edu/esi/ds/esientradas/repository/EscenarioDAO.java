package edu.esi.ds.esientradas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.esi.ds.esientradas.model.Escenario;

@Repository
public interface EscenarioDAO extends JpaRepository<Escenario, Long> {

}