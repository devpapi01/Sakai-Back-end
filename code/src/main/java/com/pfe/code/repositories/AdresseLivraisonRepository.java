package com.pfe.code.repositories;

import com.pfe.code.entities.AdresseLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdresseLivraisonRepository extends JpaRepository<AdresseLivraison, Long> {
    List<AdresseLivraison> findByMarchandId(Long marchandId);
}
