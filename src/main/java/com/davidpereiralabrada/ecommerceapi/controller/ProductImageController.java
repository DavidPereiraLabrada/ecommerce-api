package com.davidpereiralabrada.ecommerceapi.controller;

import com.davidpereiralabrada.ecommerceapi.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/images")
public class ProductImageController {

    private final FileStorageService storageService;
    private final Path root = Paths.get("uploads");

    public ProductImageController(FileStorageService storageService) {
        this.storageService = storageService;
        this.storageService.init();
    }

    // SUBIR IMAGEN: Solo accesible para ADMIN
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "customImageName", required = false) String customImageName) {
        String filename = storageService.save(file, customImageName);
        return ResponseEntity.ok("Image uploaded successfully. Saved name: " + filename);
    }

    // VER IMAGEN: Público
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}