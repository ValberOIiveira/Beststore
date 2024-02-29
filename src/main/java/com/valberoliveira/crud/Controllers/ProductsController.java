package com.valberoliveira.crud.Controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.valberoliveira.crud.Services.ProductsRepository;
import com.valberoliveira.crud.models.Product;
import com.valberoliveira.crud.models.ProductDTO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {
    @Autowired
    private ProductsRepository repo;

    @GetMapping({ "", "/" })
    public String showProductList(Model model) {
        // Sort.by(Sort.Direction.DESC,"id") to order the older to newest
        List<Product> products = repo.findAll();
        model.addAttribute("products", products);

        return "products/index";

    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDTO productDTO = new ProductDTO();
        model.addAttribute("productDto", productDTO);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result) {
        if (productDTO.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "The image file is empty"));
        }
        if (result.hasErrors()) {
            return "products/CreateProduct";
        }

        MultipartFile image = productDTO.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);

            }
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);

            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setDescription(productDTO.getDescription());
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);

        return "redirect:/products";

    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            ProductDTO productDto = new ProductDTO();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());

            model.addAttribute("productDto", productDto); // Use "productDto" em vez de "productDTO"
        } catch (Exception ex) {
            System.out.println("Exception " + ex.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "products/EditProduct";
            }

            if (!productDTO.getImageFile().isEmpty()) {
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                } catch (Exception e) {

                    System.out.println("Exception " + e.getMessage());

                }

                MultipartFile image = productDTO.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "-" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);

                }

                product.setImageFileName(storageFileName);
            }

            product.setName(productDTO.getName());
            product.setBrand(productDTO.getBrand());
            product.setCategory(productDTO.getCategory());
            product.setPrice(productDTO.getPrice());
            product.setDescription(productDTO.getDescription());

            repo.save(product);

        } catch (Exception ex) {
            System.out.println("Exception " + ex.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            // Verifica se o produto com o ID fornecido existe no banco de dados
            Product product = repo.findById(id).get();

            if (product != null) {
                // Corrigindo o caminho do arquivo de imagem (adicionando uma barra após
                // "public/images")
                Path imagePath = Paths.get("public/images/" + product.getImageFileName());

                try {
                    Files.delete(imagePath);
                } catch (IOException ex) {
                    // Logar ou lidar com a exceção de exclusão de arquivo
                    System.err.println("Error deleting image file: " + ex.getMessage());
                }

                repo.delete(product);
            } else {
                // Produto não encontrado, pode querer lidar com isso de alguma forma
                System.err.println("Product with ID " + id + " not found.");
            }
        } catch (Exception ex) {
            // Logar ou lidar com a exceção geral
            System.err.println("Exception during product deletion: " + ex.getMessage());
        }

        return "redirect:/products";
    }

}
