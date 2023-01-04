package grakkit;

import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueFunction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

public class GrakkitCommand extends Command {

   public static final HashMap<String, HashMap<String, GrakkitCommand>> commandCache = new HashMap<>();
   public static CommandMap commandMap;

   public V8ValueFunction executor;
   public V8ValueFunction tabCompleter;

   public GrakkitCommand (String namespace, String name, String[] aliases) {
      super(name, "", "", Arrays.asList(aliases));
      GrakkitCommand.commandMap.register(namespace, this);
   }

   @Override
   public boolean execute (CommandSender sender, String label, String[] args) {
      if (this.executor != null) {
         try {
            this.executor.invokeVoid("", sender, label, args);
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }
      return true;
   }

   @Override
   public LinkedList<String> tabComplete (CommandSender sender, String alias, String[] args) {
      LinkedList<String> output = new LinkedList<>();
      if (this.tabCompleter != null) {
         try {
            V8ValueArray input = this.tabCompleter.invoke("", sender, alias, args);
            for (long index = 0; index < input.getLength(); index++) {
               output.add(input.get(index).toString());
            }
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }
      return output;
   }
}
