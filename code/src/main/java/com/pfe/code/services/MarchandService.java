package com.pfe.code.services;

import com.pfe.code.entities.Fournisseur;
import com.pfe.code.entities.Marchand;
import com.pfe.code.entities.Produit;
import com.pfe.code.services.request.Register;

import java.util.List;
import java.util.Optional;
//principe SRP
public interface MarchandService {
    List<Marchand>getAll();
    Marchand createMarchand(Register register);
    Marchand updateMarchand(Marchand marchand);
    public void sendEmailUser(Marchand marchand, String code);
    public Marchand validateToken(String code);
    List<Marchand>getByNomContains(String nom);
    List<Marchand>getByNomAsc();
    List<Marchand>getByNomDESC();
    List<Marchand>getByPreAcs();

    Optional<Marchand> findByEmail(String email);

    List<Marchand>getByPreDesc();


    void deleteMarchandById(Long id);

}
