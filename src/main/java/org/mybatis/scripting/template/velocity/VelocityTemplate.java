package org.mybatis.scripting.template.velocity;

import org.apache.velocity.VelocityContext;
import org.mybatis.scripting.template.Template;
import org.mybatis.scripting.template.TemplateArguments;
import org.mybatis.scripting.template.TemplateException;

import java.io.Writer;
import java.util.Map;

/**
 * 针对Velocity模板的包装
 *
 * @see org.apache.velocity.Template
 */
class VelocityTemplate implements Template {

  org.apache.velocity.Template template;

  public VelocityTemplate(org.apache.velocity.Template template) {
    this.template = template;
  }

  @Override
  public String getName() {
    return template.getName();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void render(Object dataModel, Writer writer) throws TemplateException {
    if (dataModel instanceof TemplateArguments) {
      template.merge(new VelocityContext(((TemplateArguments) dataModel).asMap()), writer);
    } else if (dataModel instanceof Map) {
      template.merge(new VelocityContext((Map<String, Object>) dataModel), writer);
    } else {
      throw new TemplateException("data model is not supported.");
    }
  }
}
