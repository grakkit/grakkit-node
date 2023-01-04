package grakkit;

public class JavaAPI {
   public Class<?> type(String type) throws ClassNotFoundException {
      return Class.forName(type);
   }
}
