package com.group01.asm2.utils;

import com.cloudinary.Cloudinary;
import com.group01.asm2.configs.CloudinaryConfig;

import java.io.File;
import java.util.Map;

public class CloudinaryUploaderUtil {

  public static String uploadImage(File file){

    try {

      Cloudinary cloudinary =
          CloudinaryConfig.getClient();

      Map result =
          cloudinary.uploader().upload(
              file,
              Map.of(
                  "folder", "asm2",
                  "resource_type", "auto"
              )
          );

      return result.get("secure_url").toString();

    } catch (Exception e){

      throw new RuntimeException(
          "Cloudinary upload failed: " + e.getMessage()
      );
    }
  }
}