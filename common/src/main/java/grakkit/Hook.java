package grakkit;

public class Hook {

   public static enum HookType { CloseEnd, CloseStart, OpenEnd, OpenStart, Reload, Tick }

   public Boolean once;
   public Runnable runnable;
   public HookType type;

   public Hook (HookType type, Runnable runnable, Boolean once) {
      this.once = once;
      this.runnable = runnable;
      this.type = type;
      Grakkit.hooks.add(this);
   }

   public void destroy () {
      if (this.runnable != null) {
         Grakkit.hooks.remove(this);
         this.runnable = null;
      }
   }

   public void execute (HookType type) {
      if (type == this.type) {
         this.runnable.run();
         if (this.once) {
            this.destroy();
         }
      }
   }
}
