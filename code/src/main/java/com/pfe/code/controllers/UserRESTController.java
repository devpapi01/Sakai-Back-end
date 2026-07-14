package com.pfe.code.controllers;

import com.pfe.code.entities.Utilisateur;
import com.pfe.code.security.SecurityUtils;
import com.pfe.code.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserRESTController {
    @Autowired
    UserService userService;

    private void checkIsSelf(Long targetId) {
        Utilisateur current = userService.getByEmail(SecurityUtils.currentEmail());
        if (current == null || !current.getId().equals(targetId)) {
            throw new AccessDeniedException("Vous ne pouvez agir que sur votre propre compte");
        }
    }

    @GetMapping("/all")
     public List<Utilisateur> getAll(){
        return userService.getAll();
    }

    @GetMapping("/email/{email}")
    public  Utilisateur getByemail(@PathVariable("email") String email){
        return userService.getByEmail(email);
    }

    @GetMapping("/nomcont/{nom}")
    public List<Utilisateur>getnomContains(@PathVariable("nom") String nom){
        return userService.getByNomcontains(nom);
    }
    @DeleteMapping("/deleteUser/{id}")
    public void deleteUser(@PathVariable("id")Long id){
        userService.deleteUserByid(id);
    }

    @PutMapping("/changepassword/{id}")
    public Utilisateur change(@PathVariable("id")Long id,@RequestBody String change){
       checkIsSelf(id);
       return userService.changepasseword(id,change);
    }

    @PutMapping("/updateinfosuser")
    public Utilisateur update(@RequestBody Utilisateur u){
    checkIsSelf(u.getId());
    return userService.updateinfos(u);
    }
}
