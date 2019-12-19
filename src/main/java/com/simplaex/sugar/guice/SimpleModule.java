package com.simplaex.sugar.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import lombok.Value;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * A supposedly simple way to bind simple bindings with very little boilerplate.
 * <p>
 * A SimpleModule allows for passing in overrides, which makes sense if you want your implementing module to
 * be extended from (for example by a TestModule which might want to replace some bindings with custom Mocks).
 * <p>
 * This module binds everything as eager singletons.
 * <p>
 * Sophisticated bindings can be given by @Provides methods. The beauty of this approach is that those methods
 * can be @Override'n (thus everything can be overriden and customized by a TestModule).
 */
public class SimpleModule extends AbstractModule {

  private final List<Binding<?>> bindings;

  public SimpleModule(final Binding<?>... bindings) {
    this.bindings = Collections.unmodifiableList(Arrays.stream(bindings).collect(Collectors.toList()));
  }

  @Value
  public static class Binding<T> {
    private final Key<T> key;
    private final Class<? extends T> implementor;
    private final Callable<T> instanceSupplier;

    private final AtomicReference<T> instance = new AtomicReference<>();

    T getInstance() {
      synchronized (instance) {
        T instance = this.instance.get();
        if (instance == null) {
          try {
            instance = instanceSupplier.call();
          } catch (final Exception exc) {
            throw new RuntimeException("Could not instantiate " + key, exc);
          }
          this.instance.set(instance);
        }
        return instance;
      }
    }
  }

  @Nonnull
  public static <T> Binding binding(
    final @Nonnull Class<T> clazz,
    final @Nonnull Class<? extends Annotation> annotation,
    final @Nonnull Class<? extends T> implementor
  ) {
    return new Binding<>(Key.get(clazz, annotation), implementor, null);
  }

  @Nonnull
  public static <T> Binding binding(final @Nonnull Class<T> clazz, final @Nonnull Class<? extends T> implementor) {
    return new Binding<>(Key.get(clazz), implementor, null);
  }

  @Nonnull
  public static <T> Binding instance(
    final @Nonnull Class<T> clazz,
    final @Nonnull Class<? extends Annotation> annotation,
    final T instance
  ) {
    return new Binding<>(Key.get(clazz, annotation), null, () -> instance);
  }

  @Nonnull
  public static <T> Binding instance(final @Nonnull Class<T> clazz, final T instance) {
    return new Binding<>(Key.get(clazz), null, () -> instance);
  }

  @Nonnull
  public static <T> Binding provide(
    final @Nonnull Class<T> clazz,
    final @Nonnull Class<? extends Annotation> annotation,
    final Callable<T> instanceSupplier
  ) {
    return new Binding<>(Key.get(clazz, annotation), null, instanceSupplier);
  }

  @Nonnull
  public static <T> Binding provide(final @Nonnull Class<T> clazz, final Callable<T> instanceSupplier) {
    return new Binding<>(Key.get(clazz), null, instanceSupplier);
  }

  private <T> void bind(final @Nonnull Binding<T> binding) {
    if (binding.getImplementor() == null) {
      try {
        bind(binding.getKey()).toInstance(binding.getInstance());
      } catch (final Exception exc) {
        throw new RuntimeException(exc);
      }
    } else {
      bind(binding.getKey()).to(binding.getImplementor()).asEagerSingleton();
    }
  }

  @Override
  protected final void configure() {
    bindings.forEach(this::bind);
  }

  @Nonnull
  public static Module module(final Binding<?>... bindings) {
    return new SimpleModule(bindings) {
    };
  }

}
