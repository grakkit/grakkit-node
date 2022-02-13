package grakkit;

import com.caoccao.javet.exceptions.JavetException;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileInstance extends Instance {

   /** The main path of this instance, which ideally points to a JavaScript file. */
   public String main;
   
   /** Builds a new file-based instance from the given paths. */
   public FileInstance (String root, String main, String meta) throws JavetException {
      super(root, meta);
      this.main = main;
   }

   /** Executes this InstanceFile */
   @Override
   public void execute () throws IOException, JavetException {
      Path index = Path.of(this.root).resolve("index.js");
      if (index.toFile().exists()) {
         this.context.getExecutor(Files.readString(index).toString()).executeVoid();
      } else {
         index.toFile().createNewFile();
      }
   }
}
