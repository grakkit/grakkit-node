package grakkit;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.LinkedList;

import com.caoccao.javet.exceptions.JavetException;

import com.caoccao.javet.interop.NodeRuntime;

import com.caoccao.javet.interop.converters.JavetProxyConverter;

import com.caoccao.javet.interop.engine.IJavetEngine;

public class Instance {

   public static enum InstanceType { File, Script }
   
   public final LinkedList<Instance> children = new LinkedList<>();
   public IJavetEngine<NodeRuntime> engine;
   public String meta;
   public Instance parent;
   public String root;
   public NodeRuntime runtime;
   public String source;
   public InstanceType type;

   public Instance (InstanceType type, String root, String source, String meta, Instance parent) throws JavetException {
      this.engine = Grakkit.pool.getEngine();
      this.meta = meta;
      this.parent = parent;
      this.root = root;
      this.source = source;
      this.type = type;
      if (this.parent != null) {
         this.parent.children.add(this);
      }
   }

   public void close () {
      if (this.runtime != null) {
         this.children.forEach(child -> child.destroy());
         try {
            this.runtime.close();
         } catch (Throwable error) {
            this.runtime.terminateExecution();
         }
         this.runtime = null;
      }
   }

   public void destroy () {
      if (this.engine != null) {
         this.close();
         if (this.parent != null) {
            this.parent.children.remove(this);
         }
         try {
            this.engine.close();
            this.engine.sendGCNotification();
            this.engine = null;
         } catch (Throwable error) {
            this.engine = null;
            error.printStackTrace();
         }
      }
   }

   public void execute () throws IOException {
      if (this.type == InstanceType.File) {
         Path path = Paths.get(this.root, this.source);
         File file = path.toFile();
         if (file.exists()) {
            String code = Files.readString(path).toString();
            try {
               this.runtime.getExecutor(code).executeVoid();
            } catch (Throwable error) {
               error.printStackTrace();
            }
         } else {
            file.createNewFile();
         }
      } else if (this.type == InstanceType.Script) {
         try {
            this.runtime.getExecutor(this.source).executeVoid();
         } catch (Throwable error) {
            error.printStackTrace();
         }
      }
   }

   public void open () throws IOException, JavetException {
      if (this.runtime == null) {
         try {
            this.runtime = this.engine.getV8Runtime();
            this.runtime.setConverter(new JavetProxyConverter());
            this.runtime.getGlobalObject().set("Java", new JavaAPI());
            this.runtime.getGlobalObject().set("Grakkit", new GrakkitAPI(this));
            this.execute();
         } catch (Throwable error) {
            this.close();
            throw error;
         }
      }
   }
}
