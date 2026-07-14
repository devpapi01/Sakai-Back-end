package com.pfe.code.services.impl;

import com.pfe.code.entities.Administrateur;
import com.pfe.code.entities.Fournisseur;
import com.pfe.code.entities.Produit;
import com.pfe.code.entities.Role;
import com.pfe.code.repositories.AdminRepo;
import com.pfe.code.repositories.FournisseurRepository;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.FournisseurService;
import com.pfe.code.services.ProduitService;
import com.pfe.code.services.utils.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.pfe.code.services.tools.EmailMessage.toFournisseur;

@Service
public class FournisseurServiceImpl implements FournisseurService {
    @Autowired
    FournisseurRepository fournisseurRepository;

    @Autowired
    ProduitService produitService;

    @Autowired
    EmailSender emailSender;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    AdminRepo adminRepo;

    @Override
    public List<Fournisseur> getAll() {
        return fournisseurRepository.findAll();
    }

    @Override
    public List<String> getAllnoms() {
        return fournisseurRepository.nomsfournisseurs();
    }

    @Override
    public Fournisseur saveFournisseur(Fournisseur fournisseur) {
        fournisseur.setProduits(new ArrayList<Produit>());
        fournisseur.setRole(Role.FOURNISSEUR);
        fournisseur.setPassword(bCryptPasswordEncoder.encode(fournisseur.getPassword()));
        this.sendEmailUser(fournisseur);
        return fournisseurRepository.save(fournisseur);
    }

    public void sendEmailUser(Fournisseur fournisseur) {
        String emailBody ="Bonjour "+ "<h2>"+fournisseur.getNom()+"</h2> " +toFournisseur;

        emailSender.sendEmail(fournisseur.getEmail(), emailBody);
    }

    @Override
    public Fournisseur getByid(Long id)  {
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Fournisseur with ID " + id + " not found"));
    }

    @Override
    public List<Fournisseur> findByAdresse_Pays(String pays) {
        return fournisseurRepository.findByAdresse_Pays(pays);
    }

    @Override
    public List<Fournisseur> getByNomContains(String nom) {
        return fournisseurRepository.findByNomContains(nom);
    }

    @Override
    public List<Fournisseur> getByNomACS() {
return fournisseurRepository.trierOrderByNomASC();
    }

    @Override
    public List<Fournisseur> getByNomDESC() {
        return fournisseurRepository.trierOrderByNomDESC();
    }

    @Override
    public List<Fournisseur> getByPreAcs() {
        return fournisseurRepository.trierOrderByPreASC();
    }

    @Override
    public Optional<Fournisseur> findByEmail(String email) {
        return fournisseurRepository.findByEmail(email);
    }

    @Override
    public Administrateur addadmin(Administrateur administrateur) {
        administrateur.setPassword(bCryptPasswordEncoder.encode(administrateur.getPassword()));
        administrateur.setRole(Role.ADMIN);
        return adminRepo.save(administrateur);
    }

    @Override
    public List<Fournisseur> getByPreDesc() {
        return fournisseurRepository.trierOrderByPreDESC();
    }



    @Override
    public void deleteFournisseurById(Long id) {
        fournisseurRepository.deleteById(id);

    }



    @Override
    public Fournisseur updateinfoFour( Fournisseur fournisseurUp) {
        Optional<Fournisseur> fournisseur= fournisseurRepository.findById(fournisseurUp.getId());
        if (fournisseur.isEmpty()) {
            throw new GlobalException("Fournisseur not found");

        }
        else {
            fournisseur.get().setNom(fournisseurUp.getNom());
            fournisseur.get().setPrenom(fournisseurUp.getPrenom());
            fournisseur.get().setDescription(fournisseurUp.getDescription());
            fournisseur.get().setTelephone(fournisseurUp.getTelephone());
           fournisseur.get().setAdresse(fournisseurUp.getAdresse());
           fournisseur.get().setEmail(fournisseurUp.getEmail());
            return fournisseurRepository.save(fournisseur.get());
        }


    }

    @Override
    public Fournisseur updateFourbyid(Long id, Produit produit) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Fournisseur with ID " + id + " not found"));
        List<Produit> produits= fournisseur.getProduits();
        produits.add(produit);
        produit.setFournisseur(fournisseur);
        return fournisseurRepository.save(fournisseur);
    }




}
