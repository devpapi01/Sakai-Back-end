package com.pfe.code.services.impl;

import com.pfe.code.entities.Utilisateur;
import com.pfe.code.repositories.UtilisateurRepository;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.UserService;
import com.pfe.code.services.utils.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    EmailSender emailSender;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Override
    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }

    @Override
    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Override
    public List<Utilisateur> getByNomcontains(String nom) {
        return utilisateurRepository.findByNomContains(nom);
    }



    @Override
    public Utilisateur changepasseword(Long id,String change) {
        Optional<Utilisateur>optional = utilisateurRepository.findById(id);
        if (optional.isEmpty())
            throw new GlobalException("Utilisateur non trouvé ");
        optional.get().setPassword(bCryptPasswordEncoder.encode(change));


        return utilisateurRepository.save(optional.get());
    }

    @Override
    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Override
    public void deleteUserByid(Long id) {
        utilisateurRepository.deleteById(id);
    }

    @Override
    public Utilisateur updateinfos(Utilisateur utilisateur) {
        Optional<Utilisateur> utilisateur1= utilisateurRepository.findById(utilisateur.getId());
        if (utilisateur1.isEmpty())
            throw new GlobalException("Utilisateur non trouvé ");
        utilisateur1.get().setNom(utilisateur.getNom());
        utilisateur1.get().setPrenom(utilisateur.getPrenom());
        utilisateur1.get().setEmail(utilisateur.getEmail());
        utilisateur1.get().setAdresse(utilisateur.getAdresse());
        utilisateur1.get().setTelephone(utilisateur.getTelephone());

        return utilisateurRepository.save(utilisateur1.get());
    }
}
