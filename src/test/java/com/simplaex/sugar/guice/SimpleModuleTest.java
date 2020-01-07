package com.simplaex.sugar.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.simplaex.sugar.guice.SimpleModule.*;

public class SimpleModuleTest {

  @Test
  public void testSimpleModule() {
    final Injector injector = Guice.createInjector(module(
      binding(List.class, ArrayList.class),
      instance(Integer.class, 7),
      instance(Long.class, 4711L)
    ));

    final List<?> list1 = injector.getProvider(List.class).get();
    final List<?> list2 = injector.getProvider(List.class).get();

    Assert.assertSame(
      "instances should be singletons",
      list1,
      list2
    );
    Assert.assertEquals(
      "should have bound a specific instance for Integer.class",
      injector.getProvider(Integer.class).get(),
      Integer.valueOf(7)
    );
    Assert.assertEquals(
      "should have bound a specific instance of Long.class",
      injector.getProvider(Long.class).get(),
      Long.valueOf(4711L)
    );
  }
}
