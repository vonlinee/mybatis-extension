package org.mybatis.scripting.velocity;

import org.apache.velocity.runtime.directive.Directive;

public abstract class BlockDirective extends Directive {

  @Override
  public final int getType() {
    return Directive.BLOCK;
  }
}
