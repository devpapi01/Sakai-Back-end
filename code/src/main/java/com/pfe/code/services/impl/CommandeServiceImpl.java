package com.pfe.code.services.impl;

import com.pfe.code.entities.*;
import com.pfe.code.repositories.*;
import com.pfe.code.services.CommandeService;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.MarchandService;
import com.pfe.code.services.utils.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.pfe.code.services.tools.EmailMessage.toFournisseur;

@Service

public class CommandeServiceImpl implements CommandeService {
    @Autowired
    CommandeRepository commandeRepository;

    @Autowired
    MarchandRepository marchandRepository;

    @Autowired
    ServiceLivraisonRepository serviceLivraisonRepository;

    @Autowired
    ProduitRepository produitRepository;

    @Autowired
    LivreurRepository livreurRepository;

    @Autowired
    EmailSender emailSender;


    public String generateCommandeReference(String nomMarchand, String nomSL,String date) {

        String m = nomMarchand.substring(0, Math.min(nomMarchand.length(), 2));
        String sl= nomSL.substring(0,Math.min(nomSL.length(), 2));

        String ref= m+sl+date;

        return ref;
    }

    @Override
    public List<Commande> findAll() {
        return commandeRepository.findAll();
    }

    @Override
    public Commande findById(Long id) {
        return commandeRepository.findById(id).orElse(null);
    }

    @Override
    public List<Commande> findByMarchandId(Long id) {
        return commandeRepository.findByMarchandId(id);
    }

    @Override
    public List<Commande> findBySLId(Long id) {
        return commandeRepository.findByServiceLivraisonId(id);
    }

    @Override
    public Commande findByref(String ref) {
        return commandeRepository.findByReference(ref);
    }

    @Override
    public List<Commande> findByLivreurId(Long id) {
        return commandeRepository.findByLivreurId(id);
    }

    @Override
    public Commande createCommande(Commande commande) {


   LocalDate date= LocalDate.now();
      java.sql.Date sqlDate = java.sql.Date.valueOf(date);


      commande.setDateCommande(sqlDate);
       String ref= generateCommandeReference(commande.getEmailRec(),commande.getNumRec(),commande.getDateCommande().toString());
       commande.setReference(ref);

       if(commande.getServiceLivraison()!=null){
           this.sendEmailUser(commande.getServiceLivraison().getEmail(),commande);
       }
      this.sendEmailUser(commande.getEmailRec(),commande);

        commande.setEtat(Etat.EN_ATTENTE);

       return commandeRepository.save(commande);

    }

    public void sendEmailUser(String email, Commande commande) {
        String emailBody ="Bonjour "+ "<h2>"+email+"</h2> "+"nouvelle commande lancée avec la reference:" +commande.getReference();

        emailSender.sendEmail(email, emailBody);
    }

    @Override
    public Commande setLivreurCommande(Long idCom, Long idLivreur) {
        Livreur livreur = livreurRepository.findById(idLivreur)
                .orElseThrow(() -> new GlobalException("Livreur introuvable"));
        Commande commande = commandeRepository.findById(idCom)
                .orElseThrow(() -> new GlobalException("Commande introuvable"));
        //email au livreur
        commande.setLivreur(livreur);
        return commandeRepository.save(commande);
    }

    @Override
    public Commande updateEtat(Long idCom, String etat) {

        //email si marchand pour l'état de sa commande
        Commande commande = commandeRepository.findById(idCom)
                .orElseThrow(() -> new GlobalException("Commande introuvable"));
        Etat etat1=Etat.fromJsonString(etat);

       commande.setEtat(etat1);


        return commandeRepository.save(commande);
    }

    @Override
    public void deleteCommandeById(Long id) {
        commandeRepository.deleteById(id);

    }
}
