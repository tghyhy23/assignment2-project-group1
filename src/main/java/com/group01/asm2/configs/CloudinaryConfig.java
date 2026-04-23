package com.group01.asm2.configs;
import com.cloudinary.Cloudinary;
import java.util.Map;

public class CloudinaryConfig {

    private static final Cloudinary cloudinary =
            new Cloudinary(
                    Map.of(
                            "cloud_name", System.getenv("CLOUDINARY_NAME"),
                            "api_key", System.getenv("CLOUDINARY_KEY"),
                            "api_secret", System.getenv("CLOUDINARY_SECRET")
                    )
            );

    public static Cloudinary getClient(){
        return cloudinary;
    }
}