/*
 *    Copyright 2012-2022 the original author or authors.
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
package org.mybatis.scripting.template.velocity;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.mybatis.scripting.template.Template;
import org.mybatis.scripting.template.TemplateArguments;
import org.mybatis.scripting.template.TemplateEngine;
import org.mybatis.scripting.template.TemplateException;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class VelocityTemplateEngine implements TemplateEngine {

  private static final RuntimeInstance engine = new RuntimeInstance();
  private static final Map<String, Object> additionalCtxAttributes = new HashMap<>();

  public VelocityTemplateEngine() {
    // Prevent instantiation
    initialize(VelocityLanguageDriverConfig.newInstance());
  }

  /**
   * Initialize a template engine.
   *
   * @param driverConfig a language driver configuration
   * @since 2.1.0
   */
  public void initialize(VelocityLanguageDriverConfig driverConfig) {
    Properties properties = new Properties();
    driverConfig.getVelocitySettings().forEach(properties::setProperty);
    properties.setProperty(RuntimeConstants.CUSTOM_DIRECTIVES, driverConfig.generateCustomDirectivesString());
    engine.init(properties);
    additionalCtxAttributes.putAll(driverConfig.getAdditionalContextAttributes().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, v -> {
        try {
          return Resources.classForName(v.getValue()).getConstructor().newInstance();
        } catch (Exception e) {
          throw new ScriptingException("Cannot load additional context attribute class.", e);
        }
      })));
  }

  public static String apply(Object template, Map<String, Object> context) {
    final StringWriter out = new StringWriter();
    context.putAll(additionalCtxAttributes);
    ((Template) template).render(new VelocityContext(context), out);
    return out.toString();
  }

  @Override
  public void evaluate(String template, Object arguments, Writer writer) throws TemplateException {
    if (arguments instanceof TemplateArguments) {
      VelocityContext context = new VelocityContext(((TemplateArguments) arguments).asMap());

      engine.evaluate(context, writer, "string template", template);
    }
  }

  @Override
  public Template getTemplate(String name, String template) throws TemplateException {
    StringReader reader = new StringReader(template);
    try {
      org.apache.velocity.Template templateInstance = new org.apache.velocity.Template();
      SimpleNode node = engine.parse(reader, templateInstance);
      templateInstance.setRuntimeServices(engine);
      templateInstance.setData(node);
      templateInstance.setName(name);
      templateInstance.initDocument();
      return new VelocityTemplate(templateInstance);
    } catch (Throwable throwable) {
      throw new TemplateException(throwable);
    }
  }
}
