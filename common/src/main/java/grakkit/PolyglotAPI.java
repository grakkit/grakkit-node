package grakkit;

import com.caoccao.javet.exceptions.JavetException;

import com.caoccao.javet.interop.V8ScriptOrigin;

import com.caoccao.javet.values.V8Value;

public class PolyglotAPI {

   /** The underlying instance to which this API is linked. */
   private Instance instance;

   /** Builds a new Polyglot API object around the given instance. */
   public PolyglotAPI (Instance instance) {
      this.instance = instance;
   }

   /** Evaluates the given code. */
   public V8Value eval (String language, String code) throws JavetException {
      return this.instance.context.execute(code, new V8ScriptOrigin(), false);
   }
}
