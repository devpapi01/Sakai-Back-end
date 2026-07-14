package com.pfe.code.controllers;

import com.pfe.code.entities.Image;
import com.pfe.code.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageRESTController {
    @Autowired
    ImageService imageService;

    @PostMapping(value = "/uplaodImageProd/{idProd}" )
    public Image uploadMultiImages(@RequestParam("image")MultipartFile file,
                                   @PathVariable("idProd") Long idProd)
            throws IOException {
        return imageService.uplaodImageProd(file,idProd);
    }


    @RequestMapping(value = "/get/info/{id}" , method = RequestMethod.GET)
    public Image getImageDetails(@PathVariable("id") Long id) throws IOException {
        return imageService.getImageDetails(id) ;
    }


    @RequestMapping(value = "/load/{id}" , method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id) throws IOException
    {
        return imageService.getImage(id);
    }


    @RequestMapping(value = "/delete/{id}" , method = RequestMethod.DELETE)
    public void deleteImage(@PathVariable("id") Long id){
        imageService.deleteImage(id);
    }


    @RequestMapping(value = "/getImagesProd/{idProd}" ,
            method = RequestMethod.GET)
    public List<Image> getImagesProd(@PathVariable("idProd") Long idProd)
            throws IOException {
        return imageService.getImagesParProd(idProd);
    }
}
