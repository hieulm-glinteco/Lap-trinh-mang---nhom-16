/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.net;

import com.mycompany.ltmproject.model.User;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.sql.*;
import java.util.Map;
import com.mycompany.ltmproject.util.DB;

/**
 *
 * @author TT
 */
public class UploadImage{
    public static void main(String[] args) {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dngqc1vca",
                "api_key", "961685549423634",
                "api_secret", "8bSChHX61ki6b_PIBKMPqLgsorE"
        ));
        try(Connection conn = DB.get()){
            File folder = new File("D:\\Lap-trinh-mang\\LTMProject\\src\\main\\resources\\images");
            if(!folder.exists() || !folder.isDirectory()){
                System.out.println("khong tim thay thu muc image");
                return;
            }
            
            for(File file : folder.listFiles()){
                if (file.isFile() && isImage(file)) {
                    System.out.println("🔄 Đang upload: " + file.getName());

                    try {
                        // 4.1 Upload ảnh lên Cloudinary
                        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
                        String imageUrl = (String) uploadResult.get("secure_url");

                        // 4.2 Lưu thông tin vào cơ sở dữ liệu
                        String sql = "INSERT INTO Image (number1, number2, number3, filepath) VALUES (?, ?, ?, ?)";
                        PreparedStatement ps = conn.prepareStatement(sql);

                        // ⚙️ Tùy bạn, có thể sinh 3 số hoặc đọc từ nơi khác
                        int n1 = (int) (Math.random() * 100);
                        int n2 = (int) (Math.random() * 100);
                        int n3 = (int) (Math.random() * 100);

                        ps.setInt(1, n1);
                        ps.setInt(2, n2);
                        ps.setInt(3, n3);
                        ps.setString(4, imageUrl);

                        ps.executeUpdate();
                        ps.close();

                        System.out.println("✅ Upload thành công: " + file.getName());
                        System.out.println("   URL: " + imageUrl);
                    } catch (Exception e) {
                        System.err.println("⚠️ Lỗi upload file " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private static boolean isImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }
}