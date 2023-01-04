package grakkit;

import com.caoccao.javet.values.reference.V8ValueFunction;

import java.lang.reflect.Field;

import java.sql.DriverManager;

import java.util.HashMap;

import org.bukkit.command.CommandMap;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

   @Override
   public void onLoad() {
      DriverManager.getDrivers();
      Grakkit.patch(new Loader(this.getClassLoader()));
      try {
         Field internal = this.getServer().getClass().getDeclaredField("commandMap");
         internal.setAccessible(true);
         GrakkitCommand.commandMap = (CommandMap) internal.get(this.getServer());
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
      GrakkitCommand.commandCache.values().forEach(subcache -> {
         subcache.values().forEach(command -> {
            command.executor = null;
            command.tabCompleter = null;
         });
      });
   }

   public void registerCommand (
      String namespace,
      String name,
      String[] aliases,
      V8ValueFunction executor,
      V8ValueFunction tabCompleter,
      String permission
   ) {
      GrakkitCommand command = GrakkitCommand.commandCache.computeIfAbsent(namespace, (key) -> {
         return new HashMap<>();
      }).computeIfAbsent(name, (key) -> {
         return new GrakkitCommand(namespace, name, aliases);
      });
      command.executor = executor;
      command.tabCompleter = tabCompleter;
      command.setPermission(permission);
   }
}
