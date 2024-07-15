package me.redstoner2019.graphics.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextureProvider {
    private static TextureProvider INSTANCE;
    private HashMap<String, Texture> textures = new HashMap<>();
    private List<String> missingTextures = new ArrayList<>();

    private TextureProvider(){

    }

    public static TextureProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TextureProvider();
        }
        return INSTANCE;
    }

    public Texture get(String texture){
        if(textures.containsKey(texture)) return textures.get(texture);
        else {
            if(!missingTextures.contains(texture)) System.err.println("Texture '" + texture + "' not found.");
            if(!missingTextures.contains(texture)) missingTextures.add(texture);
            return textures.get("white.png");
        }
    }
    public void put(String texturePath, Texture texture){
        textures.put(texturePath,texture);
    }
}
