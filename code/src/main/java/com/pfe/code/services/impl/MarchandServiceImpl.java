package com.pfe.code.services.impl;

import com.pfe.code.entities.Marchand;
import com.pfe.code.entities.Role;
import com.pfe.code.entities.Utilisateur;
import com.pfe.code.repositories.MarchandRepository;
import com.pfe.code.repositories.ProduitRepository;
import com.pfe.code.repositories.UtilisateurRepository;
import com.pfe.code.services.Exceptions.EmailAlreadyExistsException;
import com.pfe.code.services.Exceptions.ExpiredTokenException;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.Exceptions.InvalidTokenException;
import com.pfe.code.services.MarchandService;
import com.pfe.code.services.request.Register;
import com.pfe.code.services.request.VerificationToken;
import com.pfe.code.services.request.VerificationTokenRepository;
import com.pfe.code.services.utils.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class MarchandServiceImpl implements MarchandService {
    @Autowired
    MarchandRepository marchandRepository;

    @Autowired
    ProduitRepository produitRepository;
    @Autowired
    EmailSender emailSender;


    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    VerificationTokenRepository verificationTokenRepository;
    @Override
    public List<Marchand> getAll() {
        return marchandRepository.findAll();
    }



    @Override
    public Marchand createMarchand(Register register) {

        Utilisateur OptionalUser= utilisateurRepository.findByEmail(register.getEmail());
        if(OptionalUser!=null)
            throw new EmailAlreadyExistsException("Email deja existant");
        Marchand marchand = new Marchand();
        marchand.setEmail(register.getEmail());
        marchand.setAdresse(register.getAdresse());
        marchand.setNom(register.getNom());
        marchand.setPrenom(register.getPrenom());
        marchand.setTelephone(register.getTelephone());
        marchand.setPassword(bCryptPasswordEncoder.encode(register.getPassword()));
        marchand.setRole(Role.ACHETEUR);
        marchand.setIsactive(false);
        marchandRepository.save(marchand);
        //envoi de l'envoi de l'email
        String code = this.generateCode();
        VerificationToken token= new VerificationToken(code,marchand);
        verificationTokenRepository.save(token);
        this.sendEmailUser(marchand,code);

        return marchand;
    }

    @Override
    public Marchand updateMarchand(Marchand marchand) {
        Optional<Marchand> optional=marchandRepository.findById(marchand.getId());
        if (optional.isEmpty())
            throw new GlobalException("Le marchand n'existe pas");
        optional.get().setNom(marchand.getNom());
        optional.get().setPrenom(marchand.getPrenom());
        optional.get().setTelephone(marchand.getTelephone());
        optional.get().setAdresse(marchand.getAdresse());
        return marchandRepository.save(optional.get());
    }


    private String generateCode(){
        Random random= new Random();
        Integer code= 100000+ random.nextInt(900000);
        return code.toString();
    }
    @Override
    public void sendEmailUser(Marchand marchand, String code) {
        String emailBody ="Bonjour "+ "<h2>"+marchand.getNom()+"</h2>" +
                " Votre code de validation est "+"<h1>"+code+"</h1>";
        emailSender.sendEmail(marchand.getEmail(), emailBody);
    }


    @Override
    public List<Marchand> getByNomContains(String nom) {
        return marchandRepository.findByNomContains(nom);
    }

    @Override
    public List<Marchand> getByNomAsc() {
        return marchandRepository.trierOrderByNomASC();
    }

    @Override
    public List<Marchand> getByNomDESC() {
        return marchandRepository.trierOrderByPreDESC();
    }

    @Override
    public List<Marchand> getByPreAcs() {
        return marchandRepository.trierOrderByPreASC();
    }

    @Override
    public Optional<Marchand> findByEmail(String email) {
        return marchandRepository.findByEmail(email);
    }

    @Override
    public Marchand validateToken(String code) {
        VerificationToken token = verificationTokenRepository.findByToken(code);
        if(token == null){
            throw new InvalidTokenException("INVALID_TOKEN");
        }


        Marchand marchand= token.getMarchand();
        Calendar calendar = Calendar.getInstance();
        if ((token.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0){
            verificationTokenRepository.delete(token);
            throw new ExpiredTokenException("EXPIRED_TOKEN");
        }
        marchand.setIsactive(true);
        marchandRepository.save(marchand);
        return marchand;
    }

    @Override
    public List<Marchand> getByPreDesc() {
        return marchandRepository.trierOrderByPreDESC();
    }

    @Override
    public void deleteMarchandById(Long id) {
    marchandRepository.deleteById(id);
    }
}
