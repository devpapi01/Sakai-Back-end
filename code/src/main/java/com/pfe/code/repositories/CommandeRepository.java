package com.pfe.code.repositories;

import com.pfe.code.entities.Commande;
import com.pfe.code.entities.Etat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository


public interface CommandeRepository  extends JpaRepository<Commande,Long > {
    List<Commande>findByMarchandId(Long id);
    Commande findByReference(String ref);
    List<Commande>findByServiceLivraisonId(Long id);
    List<Commande>findByLivreurId(Long id);
    Optional<Commande> findFirstByMarchandIdAndEtat(Long marchandId, Etat etat);
}
