/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.builder.xml;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.internal.Constants;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.util.PropertiesWrapper;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class XMLConfigBuilder extends BaseBuilder {

  private boolean parsed;
  private final XPathParser parser;
  private String environment;
  private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

  private Configuration configuration;

  public XMLConfigBuilder(Reader reader) {
    this(reader, null, null);
  }

  public XMLConfigBuilder(Reader reader, String environment) {
    this(reader, environment, null);
  }

  public XMLConfigBuilder(Reader reader, String environment, Properties props) {
    this(Configuration.class, reader, environment, props);
  }

  public XMLConfigBuilder(Class<? extends Configuration> configClass, Reader reader, String environment,
                          Properties props) {
    this(configClass, new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
  }

  public XMLConfigBuilder(InputStream inputStream) {
    this(inputStream, null, null);
  }

  public XMLConfigBuilder(InputStream inputStream, String environment) {
    this(inputStream, environment, null);
  }

  public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
    this(Configuration.class, inputStream, environment, props);
  }

  public XMLConfigBuilder(Class<? extends Configuration> configClass, InputStream inputStream, String environment,
                          Properties props) {
    this(configClass, new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
  }

  private XMLConfigBuilder(Class<? extends Configuration> configClass, XPathParser parser, String environment,
                           Properties props) {
    super(newConfig(configClass));

    this.configuration = newConfig(configClass);
    ErrorContext.instance().resource("SQL Mapper Configuration");
    this.configuration.setVariables(props);
    this.parsed = false;
    this.environment = environment;
    this.parser = parser;
  }

  public void setConfiguration(@NotNull Configuration configuration) {
    this.configuration = configuration;
  }

  public Configuration parse() {
    if (parsed) {
      throw new BuilderException("Each XMLConfigBuilder can only be used once.");
    }
    parsed = true;
    parseConfiguration(parser.evalNode("/configuration"));
    return configuration;
  }

  private void parseConfiguration(XNode root) {
    try {
      // issue #117 read properties first
      propertiesElement(root.evalNode(Constants.PROPERTIES));
      Properties settings = settingsAsProperties(root.evalNode(Constants.SETTINGS));
      loadCustomVfsImpl(settings);
      loadCustomLogImpl(settings);
      typeAliasesElement(root.evalNode(Constants.TYPE_ALIASES));
      pluginsElement(root.evalNode(Constants.PLUGINS));
      objectFactoryElement(root.evalNode(Constants.OBJECT_FACTORY));
      objectWrapperFactoryElement(root.evalNode(Constants.OBJECT_WRAPPER_FACTORY));
      reflectorFactoryElement(root.evalNode(Constants.REFLECTOR_FACTORY));
      settingsElement(settings);
      // read it after objectFactory and objectWrapperFactory issue #631
      environmentsElement(root.evalNode(Constants.ENVIRONMENTS));
      databaseIdProviderElement(root.evalNode(Constants.DATABASE_ID_PROVIDER));
      typeHandlersElement(root.evalNode(Constants.TYPE_HANDLERS));
      mappersElement(root.evalNode(Constants.MAPPERS));
    } catch (Exception e) {
      throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
  }

  private Properties settingsAsProperties(XNode context) {
    if (context == null) {
      return new Properties();
    }
    Properties props = context.getChildrenAsProperties();
    // Check that all settings are known to the configuration class
    MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
    for (Object key : props.keySet()) {
      if (!metaConfig.hasSetter(String.valueOf(key))) {
        throw new BuilderException(
          "The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
      }
    }
    return props;
  }

  private void loadCustomVfsImpl(Properties props) throws ClassNotFoundException {
    String value = props.getProperty(Constants.VFS_IMPL);
    if (value == null) {
      return;
    }
    String[] classes = value.split(String.valueOf(Constants.ENGLISH_COMMA));
    for (String clazz : classes) {
      if (!clazz.isEmpty()) {
        @SuppressWarnings("unchecked")
        Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);
        configuration.setVfsImpl(vfsImpl);
      }
    }
  }

  private void loadCustomLogImpl(Properties props) {
    Class<? extends Log> logImpl = configuration.resolveClass(props.getProperty("logImpl"));
    configuration.setLogImpl(logImpl);
  }

  private void typeAliasesElement(XNode context) {
    if (context == null) {
      return;
    }
    for (XNode child : context.getChildren()) {
      if (Constants.PACKAGE.equals(child.getName())) {
        String typeAliasPackage = child.getStringAttribute(Constants.NAME);
        configuration.registerAliases(typeAliasPackage);
      } else {
        String alias = child.getStringAttribute(Constants.ALIAS);
        String type = child.getStringAttribute(Constants.TYPE);
        try {
          Class<?> clazz = Resources.classForName(type);
          if (alias == null) {
            configuration.registerAlias(clazz);
          } else {
            configuration.registerAlias(alias, clazz);
          }
        } catch (ClassNotFoundException e) {
          throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
        }
      }
    }
  }

  private void pluginsElement(XNode context) throws Exception {
    if (context != null) {
      for (XNode child : context.getChildren()) {
        String interceptor = child.getStringAttribute(Constants.INTERCEPTOR);
        Properties properties = child.getChildrenAsProperties();
        Interceptor interceptorInstance = (Interceptor) configuration.resolveClass(interceptor).getDeclaredConstructor()
          .newInstance();
        interceptorInstance.setProperties(properties);
        configuration.addInterceptor(interceptorInstance);
      }
    }
  }

  private void objectFactoryElement(XNode context) throws Exception {
    if (context != null) {
      String type = context.getStringAttribute(Constants.TYPE);
      Properties properties = context.getChildrenAsProperties();
      ObjectFactory factory = (ObjectFactory) configuration.resolveClass(type).getDeclaredConstructor().newInstance();
      factory.setProperties(properties);
      configuration.setObjectFactory(factory);
    }
  }

  private void objectWrapperFactoryElement(XNode context) throws Exception {
    if (context != null) {
      String type = context.getStringAttribute(Constants.TYPE);
      ObjectWrapperFactory factory = (ObjectWrapperFactory) configuration.resolveClass(type).getDeclaredConstructor().newInstance();
      configuration.setObjectWrapperFactory(factory);
    }
  }

  private void reflectorFactoryElement(XNode context) throws Exception {
    if (context != null) {
      String type = context.getStringAttribute(Constants.TYPE);
      ReflectorFactory factory = (ReflectorFactory) configuration.resolveClass(type).getDeclaredConstructor().newInstance();
      configuration.setReflectorFactory(factory);
    }
  }

  /**
   * 解析<properties></properties>元素
   *
   * @param context XNode
   * @throws Exception 解析出错
   */
  private void propertiesElement(XNode context) throws Exception {
    if (context == null) {
      return;
    }
    Properties defaults = context.getChildrenAsProperties();
    String resource = context.getStringAttribute("resource");
    String url = context.getStringAttribute("url");
    if (resource != null && url != null) {
      throw new BuilderException(
        "The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
    }
    if (resource != null) {
      defaults.putAll(Resources.getResourceAsProperties(resource));
    } else if (url != null) {
      defaults.putAll(Resources.getUrlAsProperties(url));
    }
    Properties vars = configuration.getVariables();
    if (vars != null) {
      defaults.putAll(vars);
    }
    parser.setVariables(defaults);
    configuration.setVariables(defaults);
  }

  private void settingsElement(Properties props) {
    PropertiesWrapper properties = new PropertiesWrapper(props);
    configuration.setAutoMappingBehavior(properties.getEnum("autoMappingBehavior", AutoMappingBehavior.class, AutoMappingBehavior.PARTIAL));
    configuration.setAutoMappingUnknownColumnBehavior(properties.getEnum("autoMappingUnknownColumnBehavior", AutoMappingUnknownColumnBehavior.class, "NONE"));
    configuration.setCacheEnabled(properties.getBoolean("cacheEnabled", true));
    configuration.setLazyLoadingEnabled(properties.getBoolean("lazyLoadingEnabled", false));
    configuration.setAggressiveLazyLoading(properties.getBoolean("aggressiveLazyLoading", false));
    configuration.setUseColumnLabel(properties.getBoolean("useColumnLabel", true));
    configuration.setUseGeneratedKeys(properties.getBoolean("useGeneratedKeys", false));
    configuration.setDefaultExecutorType(properties.getEnum("defaultExecutorType", ExecutorType.class, ExecutorType.SIMPLE));
    configuration.setDefaultStatementTimeout(properties.getInteger("defaultStatementTimeout", null));
    configuration.setDefaultFetchSize(properties.getInteger("defaultFetchSize", null));
    configuration.setDefaultResultSetType(BaseBuilder.resolveResultSetType(props.getProperty("defaultResultSetType")));
    configuration.setMapUnderscoreToCamelCase(properties.getBoolean("mapUnderscoreToCamelCase", false));
    configuration.setSafeRowBoundsEnabled(properties.getBoolean("safeRowBoundsEnabled", false));
    configuration.setLocalCacheScope(properties.getEnum("localCacheScope", LocalCacheScope.class, LocalCacheScope.SESSION));
    configuration.setJdbcTypeForNull(properties.getEnum("jdbcTypeForNull", JdbcType.class, JdbcType.OTHER));

    String lazyLoadTriggerMethods = props.getProperty("lazyLoadTriggerMethods", "equals,clone,hashCode,toString");
    configuration.setLazyLoadTriggerMethods(new HashSet<>(Arrays.asList(lazyLoadTriggerMethods.split(","))));

    configuration.setSafeResultHandlerEnabled(properties.getBoolean("safeResultHandlerEnabled", true));
    configuration.setCallSettersOnNulls(properties.getBoolean("callSettersOnNulls", false));
    configuration.setUseActualParamName(properties.getBoolean("useActualParamName", true));
    configuration.setReturnInstanceForEmptyRow(properties.getBoolean("returnInstanceForEmptyRow", false));
    configuration.setLogPrefix(properties.getString("logPrefix", null));
    configuration.setShrinkWhitespacesInSql(properties.getBoolean("shrinkWhitespacesInSql", false));
    configuration.setArgNameBasedConstructorAutoMapping(properties.getBoolean("argNameBasedConstructorAutoMapping", false));
    configuration.setNullableOnForEach(properties.getBoolean("nullableOnForEach", false));


    configuration.setProxyFactory((ProxyFactory) configuration.createInstance(props.getProperty("proxyFactory")));
    configuration.setDefaultScriptingLanguage(configuration.resolveClass(props.getProperty("defaultScriptingLanguage")));
    configuration.setDefaultEnumTypeHandler(configuration.resolveClass(props.getProperty("defaultEnumTypeHandler")));
    configuration.setConfigurationFactory(configuration.resolveClass(props.getProperty("configurationFactory")));
    configuration.setDefaultSqlProviderType(configuration.resolveClass(props.getProperty("defaultSqlProviderType")));
  }

  private void environmentsElement(XNode context) throws Exception {
    if (context == null) {
      return;
    }
    if (environment == null) {
      environment = context.getStringAttribute(Constants.DEFAULT);
    }
    for (XNode child : context.getChildren()) {
      String id = child.getStringAttribute(Constants.ID);
      if (isSpecifiedEnvironment(id)) {
        TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
        DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
        DataSource dataSource = dsFactory.getDataSource();
        Environment.Builder environmentBuilder = new Environment.Builder(id).transactionFactory(txFactory)
          .dataSource(dataSource);
        configuration.setEnvironment(environmentBuilder.build());
        break;
      }
    }
  }

  private void databaseIdProviderElement(XNode context) throws Exception {
    if (context == null) {
      return;
    }
    String type = context.getStringAttribute(Constants.TYPE);
    // awful patch to keep backward compatibility
    if (Constants.VENDOR.equals(type)) {
      type = Constants.DB_VENDOR;
    }
    Properties properties = context.getChildrenAsProperties();
    DatabaseIdProvider databaseIdProvider = (DatabaseIdProvider) configuration.resolveClass(type).getDeclaredConstructor()
      .newInstance();
    databaseIdProvider.setProperties(properties);
    Environment environment = configuration.getEnvironment();
    if (environment != null) {
      String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());
      configuration.setDatabaseId(databaseId);
    }
  }

  private TransactionFactory transactionManagerElement(XNode context) throws Exception {
    if (context != null) {
      String type = context.getStringAttribute(Constants.TYPE);
      Properties props = context.getChildrenAsProperties();
      TransactionFactory factory = (TransactionFactory) configuration.resolveClass(type).getDeclaredConstructor().newInstance();
      factory.setProperties(props);
      return factory;
    }
    throw new BuilderException("Environment declaration requires a TransactionFactory.");
  }

  private DataSourceFactory dataSourceElement(XNode context) throws Exception {
    if (context != null) {
      String type = context.getStringAttribute(Constants.TYPE);
      Properties props = context.getChildrenAsProperties();
      DataSourceFactory factory = (DataSourceFactory) configuration.resolveClass(type).getDeclaredConstructor().newInstance();
      factory.setProperties(props);
      return factory;
    }
    throw new BuilderException("Environment declaration requires a DataSourceFactory.");
  }

  private void typeHandlersElement(XNode context) {
    if (context == null) {
      return;
    }
    for (XNode child : context.getChildren()) {
      if (Constants.PACKAGE.equals(child.getName())) {
        String typeHandlerPackage = child.getStringAttribute(Constants.NAME);
        configuration.registerTypeHandler(typeHandlerPackage);
      } else {
        String javaTypeName = child.getStringAttribute(Constants.JAVA_TYPE);
        String jdbcTypeName = child.getStringAttribute(Constants.JDBC_TYPE);
        String handlerTypeName = child.getStringAttribute(Constants.HANDLER);
        Class<?> javaTypeClass = configuration.resolveClass(javaTypeName);
        JdbcType jdbcType = configuration.resolveJdbcType(jdbcTypeName);
        Class<?> typeHandlerClass = configuration.resolveClass(handlerTypeName);
        if (javaTypeClass != null) {
          if (jdbcType == null) {
            configuration.registerTypeHandler(javaTypeClass, typeHandlerClass);
          } else {
            configuration.registerTypeHandler(javaTypeClass, jdbcType, typeHandlerClass);
          }
        } else {
          configuration.registerTypeHandler(typeHandlerClass);
        }
      }
    }
  }

  private void mappersElement(XNode context) throws Exception {
    if (context == null) {
      return;
    }
    for (XNode child : context.getChildren()) {
      if (Constants.PACKAGE.equals(child.getName())) {
        String mapperPackage = child.getStringAttribute(Constants.NAME);
        configuration.addMappers(mapperPackage);
      } else {
        String resource = child.getStringAttribute(Constants.RESOURCE);
        String url = child.getStringAttribute(Constants.URL);
        String mapperClass = child.getStringAttribute(Constants.CLASS);
        if (resource != null && url == null && mapperClass == null) {
          ErrorContext.instance().resource(resource);
          try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource,
              configuration.getSqlFragments());
            mapperParser.parse();
          }
        } else if (resource == null && url != null && mapperClass == null) {
          ErrorContext.instance().resource(url);
          try (InputStream inputStream = Resources.getUrlAsStream(url)) {
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url,
              configuration.getSqlFragments());
            mapperParser.parse();
          }
        } else if (resource == null && url == null && mapperClass != null) {
          Class<?> mapperInterface = Resources.classForName(mapperClass);
          configuration.addMapper(mapperInterface);
        } else {
          throw new BuilderException(
            "A mapper element may only specify a url, resource or class, but not more than one.");
        }
      }
    }
  }

  private boolean isSpecifiedEnvironment(String id) {
    if (environment == null) {
      throw new BuilderException("No environment specified.");
    }
    if (id == null) {
      throw new BuilderException("Environment requires an id attribute.");
    }
    return environment.equals(id);
  }

  private static Configuration newConfig(Class<? extends Configuration> configClass) {
    try {
      return configClass.getDeclaredConstructor().newInstance();
    } catch (Exception ex) {
      throw new BuilderException("Failed to create a new Configuration instance.", ex);
    }
  }

}
