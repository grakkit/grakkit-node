package grakkit;

import com.caoccao.javet.enums.JSRuntimeType;

import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;

import com.caoccao.javet.interop.engine.IJavetEnginePool;
import com.caoccao.javet.interop.engine.JavetEngineConfig;
import com.caoccao.javet.interop.engine.JavetEnginePool;

import com.caoccao.javet.values.reference.V8ValueFunction;

import java.net.URL;
import java.net.URLClassLoader;

import java.nio.file.Paths;

import java.util.HashMap;
import java.util.LinkedList;

public class Grakkit {
   
   public static final HashMap<String, LinkedList<V8ValueFunction>> channels = new HashMap<>();
   public static final LinkedList<Hook> hooks = new LinkedList<>();
   public static final HashMap<String, URLClassLoader> loaders = new HashMap<>();
   public static IJavetEnginePool<NodeRuntime> pool;
   public static Instance primary;
   public static LinkedList<Task> tasks = new LinkedList<>();

   public static void close () {
      Grakkit.trigger(Hook.HookType.CloseStart);
      V8Host.setLibraryReloadable(true);
      Grakkit.primary.destroy();
      Grakkit.primary = null;
      try {
         Grakkit.pool.close();
         Grakkit.pool = null;
         V8Host host = V8Host.getInstance(JSRuntimeType.Node);
         host.close();
         host.unloadLibrary();
      } catch (Throwable error) {
         error.printStackTrace();
      }
      Grakkit.trigger(Hook.HookType.CloseEnd);
   }

   public static URL locate (Class<?> clazz) {
      try {
         URL resource = clazz.getProtectionDomain().getCodeSource().getLocation();
         if (resource instanceof URL) return resource;
      } catch (Throwable error) {
         // do nothing
      }
      URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
      if (resource instanceof URL) {
         String link = resource.toString();
         String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";
         if (link.endsWith(suffix)) {
            String path = link.substring(0, link.length() - suffix.length());
            if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);
            try {
               return new URL(path);
            } catch (Throwable error) {
               // do nothing
            }
         }
      }
      return null;
   }

   public static void open (String root) {
      Grakkit.trigger(Hook.HookType.OpenStart);
      V8Host.setLibraryReloadable(true);
      Grakkit.pool = new JavetEnginePool<>(
         new JavetEngineConfig()
            .setAllowEval(true)
            .setGlobalName("globalThis")
            .setJSRuntimeType(JSRuntimeType.Node)
      );
      Paths.get(root).toFile().mkdir();
      String source = "index.js";
      Config[] configs = {
         new Config(Config.ConfigType.JSON, root, ".grakkitrc", false),
         new Config(Config.ConfigType.YAML, root, "config.yml", false),
         new Config(Config.ConfigType.JSON, root, "grakkit.json", false),
         new Config(Config.ConfigType.JSON, root, "package.json", true)
      };
      for (Config config : configs) {
         String main = config.getMain();
         if (main != null) {
            source = main;
            break;
         }
      }
      try {
         Grakkit.primary = new Instance(Instance.InstanceType.File, root, source, "grakkit", null);
         Grakkit.primary.open();
      } catch (Throwable error) {
         error.printStackTrace();
      }
      Grakkit.trigger(Hook.HookType.OpenEnd);
   }

   public static void patch (Loader loader) {
      try {
         loader.addURL(Grakkit.locate(Grakkit.class));
         Thread.currentThread().setContextClassLoader((ClassLoader) loader);
      } catch (Throwable error) {
         error.printStackTrace();
      }
   }

   public static void tick () {
      new LinkedList<>(Grakkit.tasks).forEach(task -> task.tick());
   }

   public static void trigger (Hook.HookType type) {
      new LinkedList<>(Grakkit.hooks).forEach(hook -> hook.execute(type));
   }
}
