/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code InterceptorChain} class manages a chain of interceptors for
 * processing target objects. It provides methods to add interceptors and
 * apply them to the target object.
 * <p>
 * This class is part of the MyBatis framework and is designed to allow
 * the implementation of cross-cutting concerns such as logging,
 * transaction management, and security in a modular way.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * InterceptorChain chain = new InterceptorChain();
 * chain.addInterceptor(new MyInterceptor());
 * Object proxiedObject = chain.pluginAll(originalObject);
 * </pre>
 *
 * @author Clinton Begin
 * @see Interceptor
 * @since 3.0.0
 */
public class InterceptorChain {

  private final List<Interceptor> interceptors = new ArrayList<>();

  /**
   * Applies all registered interceptors to the given target object.
   *
   * @param target the target object to be proxied by the interceptors
   * @return the proxied object after applying all interceptors
   */
  public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }

  public List<Interceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}
