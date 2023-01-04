package grakkit;

import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueFunction;

import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Wrapper extends Command {

   /** The executor to use for this command. */
   public V8ValueFunction executor;

   /** The tab-completer to use for this command. */
   public V8ValueFunction tabCompleter;

   /** Creates a custom command with the given options. */
   public Wrapper (String name, String[] aliases) {
      super(name, "", "", Arrays.asList(aliases));
   }

   @Override
   public boolean execute (CommandSender sender, String label, String[] args) {
      try {
         this.executor.invokeVoid("", sender, label, args);
      } catch (Throwable error) {
         // do nothing
      }
      return true;
   }

   /** Sets this wrapper's command options. */
   public void options (String permission, String message, V8ValueFunction executor, V8ValueFunction tabCompleter) {
      this.executor = executor;
      this.tabCompleter = tabCompleter;
      this.setPermission(permission);
      this.setPermissionMessage(message);
   }

   @Override
   public LinkedList<String> tabComplete (CommandSender sender, String alias, String[] args) {
      LinkedList<String> output = new LinkedList<>();
      try {
         V8ValueArray input = this.tabCompleter.invoke("", sender, alias, args);
         for (long index = 0; index < input.getLength(); index++) output.add(input.get(index).toString());
      } catch (Throwable error) {
         // do nothing
      }
      return output;
   }
}
