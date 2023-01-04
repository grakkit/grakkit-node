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
            command.setExecutor(null);
            command.setTabCompleter(null);
         });
      });
   }

   public void registerCommand (String name) {
      this.registerCommand(name, null, null, null, null, null);
   }

   public void registerCommand (String name, V8ValueFunction executor) {
      this.registerCommand(name, executor, null, null, null, null);
   }

   public void registerCommand (String name, V8ValueFunction executor, V8ValueFunction tabCompleter) {
      this.registerCommand(name, executor, tabCompleter, null, null, null);
   }

   public void registerCommand (
      String name,
      V8ValueFunction executor,
      V8ValueFunction tabCompleter,
      String permission
   ) {
      this.registerCommand(name, executor, tabCompleter, permission, null, null);
   }

   public void registerCommand (
      String name,
      V8ValueFunction executor,
      V8ValueFunction tabCompleter,
      String permission,
      String[] aliases
   ) {
      this.registerCommand(name, executor, tabCompleter, permission, aliases, null);
   }

   public void registerCommand (
      String name,
      V8ValueFunction executor,
      V8ValueFunction tabCompleter,
      String permission,
      String[] aliases,
      String namespace
   ) {
      GrakkitCommand command = GrakkitCommand.commandCache.computeIfAbsent(namespace, (key) -> {
         return new HashMap<>();
      }).computeIfAbsent(name, (key) -> {
         return new GrakkitCommand(namespace == null ? this.getName() : namespace, name, aliases == null ? new String[0] : aliases);
      });
      command.setExecutor(executor);
      command.setTabCompleter(tabCompleter);
      command.setPermission(permission);
   }
}
