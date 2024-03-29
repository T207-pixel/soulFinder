package com.example.soulFinder.services;

import com.example.soulFinder.models.Image;
import com.example.soulFinder.models.Post;
import com.example.soulFinder.models.User;
import com.example.soulFinder.repositories.PostRepository;
import com.example.soulFinder.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.geometric.PGpoint;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    private final UserRepository userRepository;

    public List<Post> listProducts(String title) {
        if (title != null) {
            List<Post> neededPosts = new ArrayList<>();
            for (Post post : postRepository.findAll()) {
                if (post.getTitle().contains(title)) {
                    neededPosts.add(post);
                }
            }
            return neededPosts;
        }
        return postRepository.findAll();
    }

    public boolean saveProduct(Principal principal, Post post, MultipartFile file1, MultipartFile file2, MultipartFile file3, String coordinates) throws IOException {
        List<Post> userPosts = postRepository.findAllByUser(getUserByPrincipal(principal));
        System.out.println(userPosts.size());
        if (userPosts.size() == 5) {
            System.out.println("I'm here 1");
            return false;
        }
        post.setUser(getUserByPrincipal(principal));
        Image image1;
        Image image2;
        Image image3;
        if (file1.getSize() != 0) {
            image1 = toImageEntity(file1);
            image1.setPreviewImage(true);
            post.addImageToProduct(image1);
        }
        if (file2.getSize() != 0) {
            image2 = toImageEntity(file2);
            post.addImageToProduct(image2);
        }
        if (file3.getSize() != 0) {
            image3 = toImageEntity(file3);
            post.addImageToProduct(image3);
        }
        if (!coordinates.isEmpty()) {
            String[] parts = coordinates.split(", ");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            System.out.println("X: " + x);
            System.out.println("Y: " + y);
            PGpoint point = new PGpoint(x, y);
            post.addCoordinates(point);
        }
        log.info("Saving new Product. Title: {}; Author email {}", post.getTitle(), post.getUser().getEmail());
        Post postFromDb = postRepository.save(post);
        postFromDb.setPreviewImageId(postFromDb.getImages().get(0).getId());
        postRepository.save(post);
        return true;
    }

    public User getUserByPrincipal(Principal principal) {
        if (principal == null) return new User();
        return userRepository.findByEmail(principal.getName());
    }

    private Image toImageEntity(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setBytes(file.getBytes());
        return image;
    }

    public void deleteProduct(Long id) {
        postRepository.deleteById(id);
    }

    public Post getProductById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public void approvePost(Long id) {
        postRepository.setTrueIsPostCheckedByAdmin(id);
    }

}
