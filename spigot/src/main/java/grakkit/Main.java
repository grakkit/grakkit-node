package grakkit;

import com.caoccao.javet.values.reference.V8ValueFunction;

import java.lang.reflect.Field;

import java.sql.DriverManager;

import java.util.HashMap;

import org.bukkit.command.CommandMap;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

   public static final HashMap<String, Wrapper> commands = new HashMap<>();
   public static CommandMap registry;

   @Override
   public void onLoad() {
      DriverManager.getDrivers();
      Grakkit.patch(new Loader(this.getClassLoader()));
      try {
         Field internal = this.getServer().getClass().getDeclaredField("commandMap");
         internal.setAccessible(true);
         Main.registry = (CommandMap) internal.get(this.getServer());
      } catch (Throwable error) {
         error.printStackTrace();
      }
   }

   @Override
   public void onEnable() {
      try {
         this.getServer().getScheduler().runTaskTimer(this, Grakkit::tick, 0, 1);
      } catch (Throwable error) {
         // none
      }
      Grakkit.open(this.getDataFolder().getPath());
   }

   @Override
   public void onDisable() {
      Grakkit.close();
      Main.commands.values().forEach(command -> {
         try {
            command.executor = Grakkit.primary.runtime.createV8ValueFunction("() => {}");
         } catch (Throwable error) {
            // do nothing
         }
         try {
            command.tabCompleter = Grakkit.primary.runtime.createV8ValueFunction("() => {}");
         } catch (Throwable error) {
            // do nothing
         }
      });
   }

   public void register (String namespace, String name, String[] aliases, String permission, String message, V8ValueFunction executor, V8ValueFunction tabCompleter) {
      String key = namespace + ":" + name;
      Wrapper command;
      if (Main.commands.containsKey(key)) {
         command = Main.commands.get(key);
      } else {
         command = new Wrapper(name, aliases);
         Main.registry.register(namespace, command);
         Main.commands.put(key, command);
      }
      command.options(permission, message, executor, tabCompleter);
   }
}
