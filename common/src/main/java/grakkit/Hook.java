package grakkit;

public class Hook {

   public static enum HookType { CloseEnd, CloseStart, OpenEnd, OpenStart, Reload, Tick }

   public Runnable callback;
   public Boolean once;
   public HookType type;

   public Hook (HookType type, Runnable callback, Boolean once) {
      this.once = once;
      this.callback = callback;
      this.type = type;
      Grakkit.hooks.add(this);
   }

   public void destroy () {
      if (this.callback != null) {
         Grakkit.hooks.remove(this);
         this.callback = null;
      }
   }

   public void execute () {
      this.callback.run();
      if (this.once) {
         this.destroy();
      }
   }
}
