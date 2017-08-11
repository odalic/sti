package cz.cuni.mff.xrg.odalic.util.logging;

/**
 * Interface used for performance logging
 * Created by Jan
 */
public interface PerformanceLogger {
  String getLog();

  void clearLog();

  <T extends Throwable> void doThrowableMethod(String actionName, ThrowableMethod<T> action) throws T;

  void doMethod(String actionName, SimpleMethod action);

  <R, T extends Throwable> R doThrowableFunction(String actionName, ThrowableFunction<R, T> action) throws T;

  <R, T1 extends Throwable, T2 extends Throwable> R doThrowableFunction2(String actionName, ThrowableFunction2<R, T1, T2> action) throws T1, T2;

  <R> R doFunction(String actionName, SimpleFunction<R> action);

  interface ThrowableMethod<T extends Throwable> {
    void get() throws T;
  }

  interface SimpleMethod {
    void get();
  }

  interface ThrowableFunction<R, T extends Throwable> {
    R get() throws T;
  }

  interface ThrowableFunction2<R, T1 extends Throwable, T2 extends Throwable> {
    R get() throws T1, T2;
  }

  interface SimpleFunction<R> {
    R get();
  }
}
