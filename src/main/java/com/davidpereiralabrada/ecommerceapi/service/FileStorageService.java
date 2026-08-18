package com.davidpereiralabrada.ecommerceapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads");

    public void init() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectory(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize folder for file uploads.");
        }
    }

    public String save(MultipartFile file, String customName) {
        try {
            // Validamos que el archivo no esté vacío
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot upload an empty file.");
            }

            String fileNameToUse;
            if (customName != null && !customName.trim().isEmpty()) {
                fileNameToUse = customName.trim();
            } else {
                fileNameToUse = file.getOriginalFilename();
            }

            // Generamos un identificador único (UUID) para evitar imágenes que comparten nombre se sobreescriban
            String filename = UUID.randomUUID() + "_" + fileNameToUse;

            // Copiamos el archivo al directorio "uploads"
            Files.copy(file.getInputStream(), this.root.resolve(filename));

            // Devolvemos únicamente el nombre del archivo para guardarlo en la base de datos
            return filename;

        } catch (Exception e) {
            throw new RuntimeException("Could not save file. Error: " + e.getMessage());
        }
    }
}