package grakkit;

public class JavaAPI {

   /** Returns the class by the given type string. */
   public Class<?> type(String type) throws ClassNotFoundException {
      return Class.forName(type);
   }
}