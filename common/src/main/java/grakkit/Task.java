package grakkit;

import com.caoccao.javet.values.reference.V8ValueFunction;

public class Task {

   public static enum TaskType { Interval, Timeout }

   public String[] args;
   public int duration;
   public int lifetime = 0;
   public V8ValueFunction script;
   public TaskType type;
   
   public Task (TaskType type, V8ValueFunction script, int duration, String... args) {
      this.args = args;
      this.duration = duration;
      this.script = script;
      this.type = type;
      Grakkit.tasks.add(this);
   }

   public void destroy () {
      if (this.script != null) {
         Grakkit.tasks.remove(this);
         this.script = null;
      }
   }

   public void tick () {
      if (++this.lifetime == this.duration) {
         try {
            this.script.invokeVoid("", (Object[]) this.args);
         } catch (Throwable error) {
            error.printStackTrace();
         }
         if (this.type == TaskType.Timeout) {
            this.destroy();
         } else if (this.type == TaskType.Interval) {
            this.lifetime = 0;
         }
      }
   }
}
