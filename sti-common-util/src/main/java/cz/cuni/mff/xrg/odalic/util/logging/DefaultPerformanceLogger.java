package cz.cuni.mff.xrg.odalic.util.logging;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DefaultPerformanceLogger implements PerformanceLogger {
  private final Map<String, Long> performanceLogs = new HashMap<>();

  @Override
  public String getLog() {
    StringBuilder builder = new StringBuilder("Performance log\n");

    synchronized (performanceLogs)
    {
      performanceLogs.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
          .forEach(entry -> builder.append(entry.getKey()).append(": ").append(entry.getValue())
                .append(" ns (").append(entry.getValue()/1000000).append(" ms)\n"));
    }

    return builder.toString();
  }

  @Override
  public void clearLog() {
    synchronized (performanceLogs)
    {
      performanceLogs.clear();
    }
  }

  @Override
  public <T extends Throwable> void doThrowableMethod(String actionName, ThrowableMethod<T> action) throws T {
    long startTime = System.nanoTime();
    try {
      action.get();
    }
    finally {
      long stopTime = System.nanoTime();
      incrementCounter(actionName, stopTime - startTime);
    }
  }

  @Override
  public void doMethod(String actionName, SimpleMethod action) {
    long startTime = System.nanoTime();
    try {
      action.get();
    }
    finally {
      long stopTime = System.nanoTime();
      incrementCounter(actionName, stopTime - startTime);
    }
  }

  @Override
  public <R, T extends Throwable> R doThrowableFunction(String actionName, ThrowableFunction<R, T> action) throws T {
    long startTime = System.nanoTime();
    try {
      return action.get();
    }
    finally {
      long stopTime = System.nanoTime();
      incrementCounter(actionName, stopTime - startTime);
    }
  }

  @Override
  public <R, T1 extends Throwable, T2 extends Throwable> R doThrowableFunction2(String actionName, ThrowableFunction2<R, T1, T2> action) throws T1, T2 {
    long startTime = System.nanoTime();
    try {
      return action.get();
    }
    finally {
      long stopTime = System.nanoTime();
      incrementCounter(actionName, stopTime - startTime);
    }
  }

  @Override
  public <R> R doFunction(String actionName, SimpleFunction<R> action) {
    long startTime = System.nanoTime();
    try {
      return action.get();
    }
    finally {
      long stopTime = System.nanoTime();
      incrementCounter(actionName, stopTime - startTime);
    }
  }

  private void incrementCounter(String actionName, long time) {
    synchronized (performanceLogs)
    {
      if (performanceLogs.containsKey(actionName)) {
        performanceLogs.replace(actionName, performanceLogs.get(actionName) + time);
      }
      else {
        performanceLogs.put(actionName, time);
      }
    }
  }
}
