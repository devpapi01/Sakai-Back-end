package com.pfe.code.controllers;

import com.pfe.code.entities.Marchand;
import com.pfe.code.security.SecurityUtils;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.MarchandService;
import com.pfe.code.services.request.Register;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marchands")
public class MarchandRESTCONTROLLER {
    @Autowired
    MarchandService marchandService;

    private void checkIsSelf(Long targetId) {
        Marchand current = marchandService.findByEmail(SecurityUtils.currentEmail()).orElse(null);
        if (current == null || !current.getId().equals(targetId)) {
            throw new AccessDeniedException("Vous ne pouvez agir que sur votre propre compte");
        }
    }


    @PostMapping("/register")
    public Marchand addMarchand(@RequestBody Register register){
        return marchandService.createMarchand(register);
    }

    @GetMapping("/all")
    public List<Marchand> getAll(){
        return marchandService.getAll();
    }



    @GetMapping("/getnc/{nom}")
    public List<Marchand>getbynomc(@PathVariable("nom")String nom){
        return marchandService.getByNomContains(nom);
    }

    @GetMapping("/nomASC")
    public List<Marchand>ordernomA(){
        return marchandService.getByNomAsc();
    }

    @GetMapping("/nomDESc")
    public List<Marchand>ordernomD(){
        return marchandService.getByNomDESC();
    }

    @GetMapping("/preASC")
    public List<Marchand>orderpA(){
        return marchandService.getByPreAcs();
    }

    @GetMapping("/preDESC")
    public List<Marchand>orderpD(){
        return marchandService.getByPreDesc();
    }

    @GetMapping("/find/{email}")
    public Marchand getByMail(@PathVariable("email") String email){
        return marchandService.findByEmail(email)
                .orElseThrow(() -> new GlobalException("Marchand introuvable"));
    }







    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id){
        checkIsSelf(id);
        marchandService.deleteMarchandById(id);
    }
    @GetMapping("/verifyEmail/{token}")
    public Marchand verifyEmail(@PathVariable("token") String token){
        return marchandService.validateToken(token);
    }

    @PutMapping("/updateinfos")
    public Marchand update(@RequestBody Marchand marchand){
        checkIsSelf(marchand.getId());
        return marchandService.updateMarchand(marchand);
    }

}
