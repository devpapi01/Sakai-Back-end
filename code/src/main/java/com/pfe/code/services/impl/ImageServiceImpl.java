package com.pfe.code.services.impl;


import com.pfe.code.entities.Image;
import com.pfe.code.entities.Produit;
import com.pfe.code.repositories.ImageRepository;
import com.pfe.code.repositories.ProduitRepository;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.ImageService;
import com.pfe.code.services.ProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    ProduitService produitService;
    @Autowired
    ProduitRepository produitRepository;
    @Override
    public Image uplaodImageProd(MultipartFile file,Long idProd) throws IOException {
        Produit p = produitRepository.findById(idProd)
                .orElseThrow(() -> new GlobalException("Produit introuvable"));
        return imageRepository.save(Image.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .image(file.getBytes())
                .produit(p).build() );
    }

    @Override
    public List<Image> getImagesParProd(Long prodId) {
        Produit p = produitRepository.findById(prodId)
                .orElseThrow(() -> new GlobalException("Produit introuvable"));
        return p.getImages();     }


    @Override
    public Image getImageDetails(Long id) throws IOException {
        Image dbImage = imageRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Image introuvable"));
        return Image.builder().id(dbImage.getId())
                .name(dbImage.getName())
                .type(dbImage.getType())
                .image(dbImage.getImage()).build() ;
    }

    @Override
    public ResponseEntity<byte[]> getImage(Long id) throws IOException {
        Image dbImage = imageRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Image introuvable"));
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf(dbImage.getType()))
                .body(dbImage.getImage());

    }

    @Override
    public void deleteImage(Long id) {
        imageRepository.deleteById(id);

    }
}
