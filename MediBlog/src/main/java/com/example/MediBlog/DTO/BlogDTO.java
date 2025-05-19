package com.example.MediBlog.DTO;


import org.springframework.web.multipart.MultipartFile;

class BlogDto {
    private String titre;
    private String contenu;
    private MultipartFile imageFile;
    private Long auteurId;

    public String getTitre() {
        return "";
    }
}
