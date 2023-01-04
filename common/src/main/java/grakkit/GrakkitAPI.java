package grakkit;

import com.caoccao.javet.exceptions.JavetException;

import com.caoccao.javet.interop.V8ScriptOrigin;

import com.caoccao.javet.values.V8Value;

import com.caoccao.javet.values.reference.V8ValueFunction;

import grakkit.Hook.HookType;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.LinkedList;
import java.util.UUID;

public class GrakkitAPI {
   
   private final Instance instance;

   public GrakkitAPI (Instance instance) {
      this.instance = instance;
   }

   public void destroy () throws JavetException {
      this.instance.destroy();
   }

   public void emit (String channel, String... data) {
      if (Grakkit.channels.containsKey(channel)) {
         new LinkedList<>(Grakkit.channels.get(channel)).forEach(listener -> {
            try {
               listener.invokeVoid("", (Object[]) data);
            } catch (Throwable error) {
               error.printStackTrace();
            }
         });
      }
   }

   public V8Value eval (String code) throws JavetException {
      return this.instance.runtime.execute(code, new byte[0], new V8ScriptOrigin(), false);
   }

   public Instance fileInstance (String path) throws JavetException {
      return this.fileInstance(path, UUID.randomUUID().toString());
   }

   public Instance fileInstance (String path, String meta) throws JavetException {
      return new Instance(Instance.InstanceType.File, this.instance.root, path, meta, this.instance);
   }

   public String getMeta () {
      return this.instance.meta;
   }

   public String getRoot () {
      return this.instance.root;
   }

   public Hook hook (String name, V8ValueFunction script) {
      return this.hook(name, script, false);
   }

   public Hook hook (String name, V8ValueFunction script, Boolean once) {
      return new Hook(HookType.valueOf(name), () -> {
         try {
            script.invokeVoid("");
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }, once);
   }

   public Task intervalTask (V8ValueFunction script, int duration, String... args) {
      return new Task(Task.TaskType.Interval, script, duration, args);
   }

   public Class<?> load (File file, String name) throws ClassNotFoundException, MalformedURLException {
      URL link = file.toURI().toURL();
      String path = file.toPath().normalize().toString();
      return Class.forName(name, true, Grakkit.loaders.computeIfAbsent(path, (key) -> new URLClassLoader(
         new URL[] { link },
         Grakkit.class.getClassLoader()
      )));
   }

   public boolean off (String channel, V8ValueFunction listener) {
      if (Grakkit.channels.containsKey(channel)) {
         return Grakkit.channels.get(channel).remove(listener); 
      } else {
         return false;
      }
   }
   
   public void on (String channel, V8ValueFunction listener) {
      Grakkit.channels.computeIfAbsent(channel, key -> new LinkedList<>()).add(listener);
   }

   public void reload () {
      Grakkit.trigger(Hook.HookType.Reload);
      Grakkit.channels.clear();
      Grakkit.loaders.clear();
      this.swap();
   }

   public Instance scriptInstance (String code) throws JavetException {
      return this.scriptInstance(code, UUID.randomUUID().toString());
   }

   public Instance scriptInstance (String code, String meta) throws JavetException {
      return new Instance(Instance.InstanceType.Script, this.instance.root, code, meta, this.instance);
   }

   public void swap () {
      new Hook(Hook.HookType.Tick, () -> {
         try {
            this.instance.open();
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }, true);
      this.instance.close();
   }

   public Task timeoutTask (V8ValueFunction script, int duration, String... args) {
      return new Task(Task.TaskType.Timeout, script, duration, args);
   }
}
