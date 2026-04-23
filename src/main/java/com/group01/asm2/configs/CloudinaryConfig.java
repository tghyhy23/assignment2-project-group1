package com.group01.asm2.configs;
import com.cloudinary.Cloudinary;
import java.util.Map;

public class CloudinaryConfig {
    private static final Cloudinary cloudinary =
        new Cloudinary(Map.of(
        "cloud_name", AppConfig.get("cloudinary.cloud_name"),
        "api_key", AppConfig.get("cloudinary.api_key"),
        "api_secret", AppConfig.get("cloudinary.api_secret")
        ));

    public static Cloudinary getClient(){
        return cloudinary;
    }
}