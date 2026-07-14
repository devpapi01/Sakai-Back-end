package com.pfe.code.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reference;
    private Date dateCommande;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "marchand_id")
    private Marchand marchand;
    private String adresseLivraison;
    private String emailRec;
    private String numRec;
    private Double prixtotal;
    @OneToMany(mappedBy = "commande" , fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<LigneCommande> lignesCommande ;



    @JsonIgnore

    @ManyToOne
    @JoinColumn(name = "service_livraison_id")
    private ServiceLivraison serviceLivraison;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "livreur_id")
    private Livreur livreur;




    @Enumerated(EnumType.STRING)
    private Etat etat;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.EN_ATTENTE;




}
