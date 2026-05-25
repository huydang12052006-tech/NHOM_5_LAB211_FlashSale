package com.mycompany.flashsale.simulation.repository;

import com.mycompany.flashsale.simulation.model.Product;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    // Đường dẫn tương đối trỏ tới file data do DataGenerator tạo ra
    private final String filePath = "data/products.csv";

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("[Thong bao]: Khong tim thay file du lieu tai " + filePath);
            return products;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    products.add(Product.fromCsvLine(line));
                } catch (Exception e) {
                    // Bo qua dong loi neu co sai sot dinh dang
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("[Loi]: Khong the doc file san pham: " + e.getMessage());
        }
        return products;
    }

    public void save(Product entity) {
        List<Product> list = findAll();
        boolean exists = false;

        // Neu da ton tai ID thi ghi de (Update), neu chua thi them vao cuoi (Create)
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(entity.getId())) {
                list.set(i, entity);
                exists = true;
                break;
            }
        }
        if (!exists) {
            list.add(entity);
        }

        // Ghi lai toan bo danh sach moi vao file CSV
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(filePath))) {
            for (Product p : list) {
                bw.write(p.toCsvLine());
                bw.newLine();
            }
        } catch (java.io.IOException e) {
            System.out.println("[Loi]: Khong the ghi du lieu vao file san pham: " + e.getMessage());
        }
    }
} // Dấu đóng class phải nằm ở cuối cùng như thế này
