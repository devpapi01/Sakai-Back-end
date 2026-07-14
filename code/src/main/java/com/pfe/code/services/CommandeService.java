package com.pfe.code.services;

import com.pfe.code.entities.Commande;
import com.pfe.code.entities.Etat;

import java.util.List;
//principe SRP
public interface CommandeService {
    List<Commande> findAll();
    Commande findById(Long id);
    List<Commande>findByMarchandId(Long id);
    List<Commande>findBySLId(Long id);
    Commande findByref(String ref);
    List<Commande>findByLivreurId(Long id);
    Commande createCommande(Commande commande);
    Commande setLivreurCommande(Long idCom, Long idLivreur);
    Commande updateEtat(Long idCom,String etat);

    void deleteCommandeById(Long id);

}
