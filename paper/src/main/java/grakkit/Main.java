package grakkit;

import com.caoccao.javet.values.reference.V8ValueFunction;

import java.lang.reflect.Field;

import java.sql.DriverManager;

import java.util.HashMap;

import org.bukkit.command.CommandMap;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

   /** A list of all registered commands. */
   public static final HashMap<String, Wrapper> commands = new HashMap<>();

   /** The internal command map used to register commands. */
   public static CommandMap registry;

   @Override
   public void onLoad() {
      // Black magic. This fixes a bug, as something is breaking SQL Integration for other plugins. 
      // See https://github.com/grakkit/grakkit/issues/14.
      DriverManager.getDrivers();
      Grakkit.patch(new Loader(this.getClassLoader())); // CORE - patch class loader with GraalJS
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
         this.getServer().getScheduler().runTaskTimer(this, Grakkit::tick, 0, 1); // CORE - run task loop
      } catch (Throwable error) {
         // none
      }
      Grakkit.init(this.getDataFolder().getPath()); // CORE - initialize
   }

   @Override
   public void onDisable() {
      Grakkit.close(); // CORE - close before exit
      Main.commands.values().forEach(command -> {
         try {
            command.executor = Grakkit.driver.context.createV8ValueFunction("() => {}");
         } catch (Throwable error) {
            // do nothing
         }
         try {
            command.tabCompleter = Grakkit.driver.context.createV8ValueFunction("() => {}");
         } catch (Throwable error) {
            // do nothing
         }
      });
   }

   /** Registers a custom command to the server with the given options. */
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
