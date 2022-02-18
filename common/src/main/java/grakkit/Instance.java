package grakkit;

import com.caoccao.javet.exceptions.JavetException;

import com.caoccao.javet.interop.NodeRuntime;

import com.caoccao.javet.interop.converters.JavetProxyConverter;

import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.engine.IJavetEnginePool;

import java.io.IOException;

import java.util.ArrayList;
import java.util.LinkedList;

public class Instance {

   /** The underlying context associated with this instance. */
   public NodeRuntime context;

   /** The engine used for all instance contexts. */
   public static IJavetEnginePool<NodeRuntime> pool;

   /** All registered unload hooks tied to this instance. */
   public final Queue hooks = new Queue();

   /** All queued messages created by this instance. */
   public final LinkedList<Message> messages = new LinkedList<>();

   /** Metadata associated with this instance. */
   public String meta;

   /** The root directory of this instance. */
   public String root;

   /** The engine of this instance. */
   public IJavetEngine<NodeRuntime> engine;

   /** All queued tasks linked to this instance. */
   public final Queue tasks = new Queue();

   /** Builds a new instance from the given paths. */
   public Instance (String root, String meta) throws JavetException {
      this.meta = meta;
      this.root = root;
      this.engine = Instance.pool.getEngine();
   }

   /** Closes this instance's context. */
   public void close () {
      NodeRuntime context = this.context;
      this.hooks.release();
      try {
         context.close();
      } catch (Throwable error) {
         context.terminateExecution();
      }
   }

   /** Closes this instance and removes it from the instance registry. */
   public void destroy () throws JavetException {
      this.close();
      Grakkit.instances.remove(this);
      this.engine.close();
      this.engine.sendGCNotification();
   }

   /** Executes this instance by calling its entry point. */
   public void execute () throws IOException, JavetException {
      // do nothing
   }

   /** Opens this instance's context. */
   public void open () throws JavetException {
      this.context = this.engine.getV8Runtime();
      this.context.setConverter(new JavetProxyConverter());
      this.context.getGlobalObject().set("Java", new JavaAPI());
      this.context.getGlobalObject().set("Grakkit", new GrakkitAPI(this));
      try {
         this.execute();
      } catch (Throwable error) {
         error.printStackTrace();
      }
   }

   /** Executes the tick loop for this instance. */
   public void tick () {
      this.tasks.release();
      new ArrayList<>(this.messages).forEach(message -> {
         this.messages.remove(message);
         if (Grakkit.channels.containsKey(message.channel)) {
            Grakkit.channels.get(message.channel).forEach(listener -> {
               try {
                  listener.invokeVoid("");
               } catch (Throwable error) {
                  // do nothing
               }
            });
         }
      });
   }
}
