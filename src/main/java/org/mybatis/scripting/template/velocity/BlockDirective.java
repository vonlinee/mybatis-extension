package org.mybatis.scripting.template.velocity;

import org.apache.velocity.runtime.directive.Directive;

abstract class BlockDirective extends Directive {

  @Override
  public final int getType() {
    return BLOCK;
  }
}
