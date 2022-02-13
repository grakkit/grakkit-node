package grakkit;

import com.caoccao.javet.values.reference.V8ValueFunction;

import java.util.ArrayList;
import java.util.LinkedList;

public class Queue {

   /** The list of values in the queue. */
   public final LinkedList<V8ValueFunction> list = new LinkedList<>();

   /** Executes and removes all values from the queue. */
   public void release () {
      new ArrayList<>(this.list).forEach(value -> {
         this.list.remove(value);
         try {
            value.invokeVoid("");
         } catch (Throwable error) {
            // do nothing
         }
      });
   }
}
