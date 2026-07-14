package com.pfe.code.services.impl;

import com.pfe.code.entities.Livreur;
import com.pfe.code.entities.Role;
import com.pfe.code.entities.ServiceLivraison;
import com.pfe.code.repositories.ServiceLivraisonRepository;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.ServiceLivraisonService;
import com.pfe.code.services.utils.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.pfe.code.services.tools.EmailMessage.toLivreur;
import static com.pfe.code.services.tools.EmailMessage.toServiceLivraison;

@Service
public class ServiceLivraisonImpl implements ServiceLivraisonService {
    @Autowired
    ServiceLivraisonRepository serviceLivraisonRepository;
    @Autowired
    EmailSender emailSender;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Override
    public List<ServiceLivraison> getAll() {
        return serviceLivraisonRepository.findAll();
    }

    @Override
    public ServiceLivraison createSl(ServiceLivraison serviceLivraison) {
        //envoi d'email au service de livraison
        serviceLivraison.setRole(Role.SERVICE_LIVRAISON);
        serviceLivraison.setPassword(bCryptPasswordEncoder.encode(serviceLivraison.getPassword()));
        this.sendEmailUser(serviceLivraison);
        return serviceLivraisonRepository.save(serviceLivraison);
    }
    public void sendEmailUser(ServiceLivraison serviceLivraison) {
        String emailBody ="Bonjour "+ "<h2>"+serviceLivraison.getNom()+"</h2> " +toServiceLivraison;

        emailSender.sendEmail(serviceLivraison.getEmail(), emailBody);
    }

    @Override
    public ServiceLivraison UpdateSL(ServiceLivraison serviceLivraison) {
        ServiceLivraison existing = serviceLivraisonRepository.findById(serviceLivraison.getId())
                .orElseThrow(() -> new GlobalException("Le service n'existe pas"));
        existing.setNom(serviceLivraison.getNom());
        existing.setPrenom(serviceLivraison.getPrenom());
        existing.setPassword(serviceLivraison.getPassword());
        existing.setAdresse(serviceLivraison.getAdresse());
        return serviceLivraisonRepository.save(existing);
    }

    @Override
    public ServiceLivraison getById(Long id) {
        return serviceLivraisonRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Service de livraison introuvable"));
    }

    @Override
    public void deleteById(Long id) {
    serviceLivraisonRepository.deleteById(id);
    }
}
