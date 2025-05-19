package com.example.MediBlog.controller;

import com.example.MediBlog.model.Blog;
import com.example.MediBlog.model.Comment;
import com.example.MediBlog.model.User;
import com.example.MediBlog.service.BlogService;
import com.example.MediBlog.services.CommentService;
import com.example.MediBlog.services.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/blogs")
public class BlogController {

    private static final Logger logger = LoggerFactory.getLogger(BlogController.class);

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Autowired
    private BlogService blogService;

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public String showAllBlogs(Model model) {
        List<Blog> blogs = blogService.getAllBlogs();
        model.addAttribute("blogs", blogs);
        return "blogs";
    }

    @GetMapping("/create_blog")
    public String showCreateBlogForm(Model model) {
        model.addAttribute("blog", new Blog());
        return "create_blog";
    }

    @PostMapping("/create_blog")
    public String handleCreateBlog(
            @RequestParam("titre") String titre,
            @RequestParam("contenu") String contenu,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        logger.debug("Début de la méthode handleCreateBlog");
        logger.debug("Titre reçu: {}", titre);
        logger.debug("Contenu reçu: longueur = {}", contenu != null ? contenu.length() : "null");
        logger.debug("Image reçue: {}", imageFile != null ? imageFile.getOriginalFilename() : "null");

        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            logger.debug("Utilisateur connecté: {}", email);

            User currentUser = CustomUserDetailsService.findByEmail(email);
            if (currentUser == null) {
                logger.error("Utilisateur non trouvé pour l'email: {}", email);
                redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé!");
                return "redirect:/blogs/create_blog";
            }

            // Créer le blog
            Blog blog = new Blog();
            blog.setTitre(titre);
            blog.setContenu(contenu);
            blog.setAuteur(currentUser);
            blog.setCreatedAt(LocalDateTime.now());

            // Traitement de l'image si présente
            if (imageFile != null && !imageFile.isEmpty()) {
                logger.debug("Traitement de l'image: {}, taille: {}", imageFile.getOriginalFilename(), imageFile.getSize());

                try {
                    String imagePath = saveImage(imageFile);
                    blog.setImage(imagePath);
                    logger.debug("Image sauvegardée avec le chemin: {}", imagePath);

                } catch (IOException e) {
                    logger.error("Erreur lors du téléchargement de l'image", e);
                    redirectAttributes.addFlashAttribute("error", "Erreur lors du téléchargement de l'image: " + e.getMessage());
                    return "redirect:/blogs/create_blog";
                }
            } else {
                logger.debug("Aucune image fournie ou image vide");
            }

            // Sauvegarder le blog
            Blog savedBlog = blogService.saveBlog(blog);
            logger.debug("Blog sauvegardé avec ID: {}", savedBlog.getId());

            redirectAttributes.addFlashAttribute("message", "Blog créé avec succès!");
            redirectAttributes.addFlashAttribute("messageType", "success");

            return "redirect:/blogs/" + savedBlog.getId();

        } catch (Exception e) {
            logger.error("Erreur inattendue lors de la création du blog", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création du blog: " + e.getMessage());
            return "redirect:/blogs/create_blog";
        }
    }

    @GetMapping("/{id}")
    public String viewBlog(@PathVariable Long id, Model model) {
        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            return "redirect:/blogs";
        }

        // Récupérer les commentaires pour ce blog
        List<Comment> comments = commentService.getCommentsByBlogId(id);

        model.addAttribute("blog", blog);
        model.addAttribute("comments", comments);
        return "view_blog";
    }

    @GetMapping("/edit/{id}")
    public String showEditBlogForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            redirectAttributes.addFlashAttribute("message", "Blog non trouvé!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs";
        }

        // Vérifier que l'utilisateur connecté est l'auteur du blog ou un admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (!blog.getAuteur().getUsername().equals(username) && !authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            redirectAttributes.addFlashAttribute("message", "Vous n'êtes pas autorisé à modifier ce blog!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs/" + id;
        }

        model.addAttribute("blog", blog);
        return "edit_blog";
    }

    @PostMapping("/edit/{id}")
    public String handleEditBlog(
            @PathVariable Long id,
            @RequestParam("titre") String titre,
            @RequestParam("contenu") String contenu,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            redirectAttributes.addFlashAttribute("message", "Blog non trouvé!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs";
        }

        // Vérifier que l'utilisateur connecté est l'auteur du blog ou un admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (!blog.getAuteur().getUsername().equals(username) && !authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            redirectAttributes.addFlashAttribute("message", "Vous n'êtes pas autorisé à modifier ce blog!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs/" + id;
        }

        // Mettre à jour les champs du blog
        blog.setTitre(titre);
        blog.setContenu(contenu);

        // Traiter la nouvelle image si fournie
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Supprimer l'ancienne image si elle existe
                if (blog.getImage() != null && !blog.getImage().isEmpty()) {
                    deleteImage(blog.getImage());
                }

                String imagePath = saveImage(imageFile);
                blog.setImage(imagePath);

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("message", "Erreur lors du téléchargement de l'image: " + e.getMessage());
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/blogs/edit/" + id;
            }
        }

        // Sauvegarder les modifications
        blogService.saveBlog(blog);

        redirectAttributes.addFlashAttribute("message", "Blog modifié avec succès!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/blogs/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteBlog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            redirectAttributes.addFlashAttribute("message", "Blog non trouvé!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs";
        }

        // Vérifier que l'utilisateur connecté est l'auteur du blog ou un admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (!blog.getAuteur().getUsername().equals(username) && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            redirectAttributes.addFlashAttribute("message", "Vous n'êtes pas autorisé à supprimer ce blog!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs/" + id;
        }

        // Supprimer l'image associée si elle existe
        if (blog.getImage() != null && !blog.getImage().isEmpty()) {
            deleteImage(blog.getImage());
        }

        // Supprimer les commentaires associés
        commentService.deleteCommentsByBlogId(id);

        // Supprimer le blog
        blogService.deleteBlog(id);

        redirectAttributes.addFlashAttribute("message", "Blog supprimé avec succès!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/blogs";
    }

    /**
     * Méthode privée pour sauvegarder une image
     */
    private String saveImage(MultipartFile imageFile) throws IOException {
        // Créer le répertoire s'il n'existe pas
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            logger.debug("Répertoire de téléchargement créé: {}", uploadDir);
        }

        // Valider le type de fichier
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Le fichier doit être une image");
        }

        // Générer un nom de fichier unique
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Le nom de fichier est requis");
        }

        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex);
        }

        String newFileName = UUID.randomUUID() + fileExtension;

        // Sauvegarder le fichier
        Path filePath = uploadDir.resolve(newFileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        logger.debug("Image sauvegardée: {}", filePath);

        // Retourner le chemin relatif pour l'affichage web
        return "/images/uploads/" + newFileName;
    }

    /**
     * Méthode privée pour supprimer une image
     */
    private void deleteImage(String imagePath) {
        if (imagePath != null && imagePath.startsWith("/images/uploads/")) {
            String filename = imagePath.substring("/images/uploads/".length());
            Path filePath = Paths.get(uploadPath).resolve(filename);
            try {
                Files.deleteIfExists(filePath);
                logger.debug("Image supprimée: {}", filePath);
            } catch (IOException e) {
                logger.error("Erreur lors de la suppression de l'image: {}", filePath, e);
            }
        }
    }

    // Getters et setters
    public CustomUserDetailsService getUserService() {
        return userService;
    }

    public void setUserService(CustomUserDetailsService userService) {
        this.userService = userService;
    }
}