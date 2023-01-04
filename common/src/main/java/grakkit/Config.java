package grakkit;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.simpleyaml.configuration.ConfigurationSection;

import org.simpleyaml.configuration.file.YamlFile;

public class Config {

   public static enum ConfigType { JSON, YAML }

   public Boolean member;
   public String root;
   public String source;
   public ConfigType type;
   
   public Config (ConfigType type, String root, String source, Boolean member) {
      this.member = member;
      this.root = root;
      this.source = source;
      this.type = type;
   }

   public String getMain () {
      Path path = Paths.get(this.root, this.source);
      File file = path.toFile();
      if (file.exists()) {
         try {
            if (this.type == ConfigType.JSON) {
               JsonObject object = Json.parse(Files.readString(path)).asObject();
               if (this.member) {
                  object = object.get("grakkit").asObject();
               }
               return object.getString("main", null);
            } else if (this.type == ConfigType.YAML) {
               ConfigurationSection object = YamlFile.loadConfiguration(file).getRoot();
               if (this.member) {
                  object = object.getConfigurationSection("grakkit");
               }
               return object.getString("main");
            }
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }
      return null;
   }
}
