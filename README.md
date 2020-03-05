# springboot-starter

## 演示自定义starter的标准创建流程。

## 使用方式

1. 在工程中添加
```xml
 <dependency>
            <groupId>com.leosun</groupId>
            <artifactId>demo-starter</artifactId>
            <version>1.0-SNAPSHOT</version>
</dependency>

2. application 中配置属性，例如

demo:
  starter:
    prefix: "hello "
    suffix: " have a nice day!"

当调用greeting方法时


    @Autowired
    DemoService  demoService;
    @GetMapping("/teststarter")
    public String teststarter(){
        return demoService.greeting("mianbaoshitou");
    }

返回
hello mianbaoshitou have a nice day

## 使用方式
创建自定义 Auto-configuration 文档翻译

## 自定义 Auto-configuration



如果在公司或者开源组织中，需要共享java库，可能会需要开发自定义的auto-configuration。将功能类打包成第三方的jar包并被springboot自动加载。

### 1. 什么是auto-configuration beans

自动装配是由 `@Configuration`注解的类。可配合`@Conditional`注解，用于指明自动加载什么情况下生效。通常情况下，自动装配会同时使用`@ConditionalOnClass`和`@ConditionalOnMissingBean`。用于确保自动装配只有在相关组件存在并且没有自定义的`@Configuration`存在的情况下才生效。

### 2. 自动装配条件的位置

Springboot会扫描jar包中的  `META-INF/spring.factories` 文件，该文件需要列出所有被`EnableAutoConfiguration`注解的类。例如

```java
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.mycorp.libx.autoconfigure.LibXAutoConfiguration,\
com.mycorp.libx.autoconfigure.LibXWebAutoConfiguration
```

> spring.factories 必须路径与文件命名均符合规定。确保该位置不会被spring扫描component时自动扫描到。且其中的类不能使用component scan。而应该使用`@Import`注解

如果 configuration 需要特定的顺序，可以使用 `@AutoConfigureAfter`或者`@AutoConfigureBefore`。例如如果要自定义web相关的configuration，那么可能自定义的类要在`WebMvcAutoConfiguration`之后再加载。

如果需要直接指定彼此之间的先后顺序，可以使用 `@AutoConfigureOrder`注解。该注解与普通的`@Order`注解作用相同但是可以用于决定 auto-configuration 的顺序。

### 3. Condition 注解

#### 3.1 类条件 Class Conditions

`@ConditionalOnClass`: 当需要的类存在时，执行`@Configuration`注解，即当某些类存在的情况下，才实例化该类的对象。

`@ConditionalOnMissingClass：当指定的类的不存在时，才会实例化本对象。

例如

```java
@Configuration
// Some conditions
public class MyAutoConfiguration {

	// Auto-configured beans
  
//当存在EmbeddedAcmeService 类时，实例化EmbeddedConfiguration
	@Configuration
	@ConditionalOnClass(EmbeddedAcmeService.class)
	static class EmbeddedConfiguration {

    //当不存在EmbeddedAcmeService 实例时，实例化该Bean
		@Bean
		@ConditionalOnMissingBean
		public EmbeddedAcmeService embeddedAcmeService() { ... }

	}

}

```

>  类条件注解中既可以使用 `Value`属性，也可以使用`name`属性。
>
> 当自定义自己的注解时需要使用到`@ConditionalOnClass`或者@ConditionalOnMissingBean时，**只能**使用name属性。

#### 3.2 Bean条件 Bean Conditions

`@ConditionalOnBean`：

`@ConditionalOnMissingBean`:

```java
@Configuration
public class MyAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public MyService myService() { ... }

}
```

myService bean 在当前ApplicationContext中不存在时，才会实例化一个myService对象。

#### 3.3 属性条件 Property Conditions

 `@ConditionalOnProperty` ：根据*Spring Environment*属性来决定是否实例化bean。

#### 3.4 资源条件 Resource Conditions

 `@ConditionalOnResource` : 根据指定的resource是否存在决定是否实例化bean。

#### 3.5 WEB应用条件 Web Application Conditions

 `@ConditionalOnWebApplication` 和 `@ConditionalOnNotWebApplication` 根据是否是web应用决定是否进行实例化bean。

#### 3.6 SpEL表达式条件 SpEL Expression Conditions

 `@ConditionalOnExpression` ：根据SpEL表达式决定是否实例化该bean。

### 4. 自动装配测试

由于有比较多的因素决定是否加载auto-configuration. 因此最好的方式是使用 `ApplicationContextRunner`来测试是否已经加载了auto-configuration.例如

```java
private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(UserServiceAutoConfiguration.class));
```

`ApplicationContextRunner` 通常用于测试类，用于收集基础、通用配置信息。

```java
@Test
public void defaultServiceBacksOff() {
	this.contextRunner.withUserConfiguration(UserConfiguration.class).run((context) -> {
		assertThat(context).hasSingleBean(UserService.class);
		assertThat(context.getBean(UserService.class))
				.isSameAs(context.getBean(UserConfiguration.class).myUserService());
	});
}

@Configuration
static class UserConfiguration {

	@Bean
	public UserService myUserService() {
		return new UserService("mine");
	}

}
```

构造`Environment`用于测试

```java
@Test
public void serviceNameCanBeConfigured() {
	this.contextRunner.withPropertyValues("user.name=test123").run((context) -> {
		assertThat(context).hasSingleBean(UserService.class);
		assertThat(context.getBean(UserService.class).getName()).isEqualTo("test123");
	});
}
```

### 5.创建自定义starter

一个完整的springboot starter包含两部分：

1. autoconfig 模块，其中包含所有的auto-configuration代码
2. starter模块，这个模块提供对autoconfig模块的依赖和其他必须的依赖。简而言之，starter应该包含所有其所必须的库。

> 也可以将两个模块合并在一个模块中。

#### 命名规则

不要用 spring-boot开头。可以将自己的项目名放在最前面，比如 acme-spring-boot-autoconfigure, acme-spring-boot-starter.

#### 属性定义

应该保证属性key的唯一性的namespace，例如不要使用 server,management,spring之类的命名空间namespace。

其他应该避免的情形

- 不要用*THE*或者*A*开头
- boolean类型的属性，使用 *Whether*或者*Enable*开头
- 集合类型，使用逗号分割
- 如果不是只能在runtime才能确定值，那么不要指明默认值(方便编译时能发现)

创建`META-INF/spring-configuration-metadata.json` 文件，这样IDE就能做代码提示。例如

```json
 {
      "name": "server.port",
      "type": "java.lang.Integer",
      "description": "Server HTTP port.",
      "sourceType": "org.springframework.boot.autoconfigure.web.ServerProperties",
      "defaultValue": 8080
    },
    {
      "name": "server.server-header",
      "type": "java.lang.String",
      "description": "Value to use for the Server response header (if empty, no header is sent).",
      "sourceType": "org.springframework.boot.autoconfigure.web.ServerProperties"
    }
```

#### autoconfigure模块

Spring Boot 使用注解处理器来收集在文件`META-INF/spring-autoconfigure-metadata.properties`。如果该文件存在，应用能快速的过滤掉不符合的auto-configurations来提高启动速度，建议在包含auto-configuration的模块中增加如下依赖

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-autoconfigure-processor</artifactId>
	<optional>true</optional>
</dependency>
```

#### starter 模块

starter模块是一个空的jar，唯一的作用在于提供starter所必要的依赖。

> 自定义的starter必须直接或者间接的要包含 `spring-boot-starter`)
