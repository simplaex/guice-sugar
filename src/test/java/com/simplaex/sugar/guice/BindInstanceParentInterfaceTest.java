package com.simplaex.sugar.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Test;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static com.simplaex.sugar.guice.SimpleModule.instance;
import static com.simplaex.sugar.guice.SimpleModule.module;

public class BindInstanceParentInterfaceTest {

  @BindInstance
  interface Iface1 {

  }

  interface Iface2 {

  }

  @BindInstance
  interface Iface3 {

  }

  @BindInstance
  interface Iface4 {

  }

  private static class Something implements Iface1, Iface2, Iface3, Iface4 {

  }

  @Test
  public void testInstanceBindingWithInterfaceBindings() {
    final Something something1 = new Something();
    final Something something2 = new Something();
    final Injector injector = Guice.createInjector(module(
      instance(Something.class, something1),
      instance(Iface3.class, something2)
    ));
    Assert.assertSame(injector.getInstance(Iface1.class), something1);
    expect(() -> injector.getInstance(Iface2.class)).toThrow(Exception.class);
    Assert.assertSame(injector.getInstance(Iface3.class), something2);
    Assert.assertSame(injector.getInstance(Iface4.class), something1);
  }
}
