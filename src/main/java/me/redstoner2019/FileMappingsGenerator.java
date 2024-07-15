package me.redstoner2019;

import me.redstoner2019.graphics.general.Util;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileMappingsGenerator {
    public static void main(String[] args) throws IOException {
        prepare();
    }

    public static void prepare() throws IOException {
        JSONObject textures = new JSONObject();
        for(String s : listFiles(new File("src/main/resources/textures").toString())){
            System.out.println(s);
            textures.put(new File(s).getName(),"textures/" + new File(s).getName());
        }

        JSONObject audios = new JSONObject();
        for(String s : listFiles(new File("src/main/resources/audio").toString())){
            System.out.println(s);
            audios.put(new File(s).getName(),"audio/" + new File(s).getName());
        }

        JSONObject data = new JSONObject();
        data.put("audio",audios);
        data.put("textures",textures);

        Util.writeStringToFile(data.toString(),new File("src/main/resources/map.json"));
    }

    public static Set<String> listFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
    }
}