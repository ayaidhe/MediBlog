package com.example.MediBlog.controller;

import com.example.MediBlog.model.Blog;
import com.example.MediBlog.model.Comment;
import com.example.MediBlog.model.User;
import com.example.MediBlog.service.BlogService;
import com.example.MediBlog.services.CommentService;
import com.example.MediBlog.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    private BlogService blogService;

    @Autowired
    private CustomUserDetailsService userService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add")
    public String addComment(
            @RequestParam("blogId") Long blogId,
            @RequestParam("content") String content,
            RedirectAttributes redirectAttributes) {

        // Récupérer le blog
        Blog blog = blogService.getBlogById(blogId);
        if (blog == null) {
            redirectAttributes.addFlashAttribute("message", "Blog non trouvé!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs";
        }

        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("message", "Utilisateur non trouvé!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs/" + blogId;
        }

        // Créer et sauvegarder le commentaire
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setBlog(blog);
        comment.setAuthor(currentUser);
        comment.setCreatedAt(LocalDateTime.now());

        commentService.saveComment(comment);

        redirectAttributes.addFlashAttribute("message", "Commentaire ajouté avec succès!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/blogs/" + blogId;
    }

    @PostMapping("/delete/{id}")
    public String deleteComment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Comment comment = commentService.getCommentById(id);
        if (comment == null) {
            redirectAttributes.addFlashAttribute("message", "Commentaire non trouvé!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs";
        }

        Long blogId = comment.getBlog().getId();

        // Vérifier que l'utilisateur connecté est l'auteur du commentaire ou un admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (!comment.getAuthor().getUsername().equals(username) && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            redirectAttributes.addFlashAttribute("message", "Vous n'êtes pas autorisé à supprimer ce commentaire!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/blogs/" + blogId;
        }

        // Supprimer le commentaire
        commentService.deleteComment(id);

        redirectAttributes.addFlashAttribute("message", "Commentaire supprimé avec succès!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/blogs/" + blogId;
    }
}