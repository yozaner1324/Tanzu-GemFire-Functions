# Using Spring in A Server-Side Function on VMware Tanzu GemFire
 
DISCLAIMER: This document describes the current (March 2020) workflow for using Spring within Tanzu GemFire and does not
necessarily reflect how this process may change in future releases of Tanzu GemFire or Spring.
 
## About This Example
 
This example project consists of three modules: function1, function2, and holder.
* Function1: A Tanzu GemFire function that uses `SpringContextBootstrappingInitializer` to create and persist an `ApplicationContext`.
* Function2: Another Tanzu GemFire function that sums the values in the /Numbers region and returns it with a greeting
message formed using two beans (“Greeting” and “Addressee”) defined in the ApplicationContext that Function1 created.
* Holder: Class to create an `ApplicationContext` and store it in a static field that can be accessed from other classes
as a singleton. holder has code for using Spring with or without Spring Boot; for this example, the Spring Boot code is
commented out. The Spring Boot dependency is also commented out in the pom.xml. holder is only relevant if NOT using
`SpringContextBootstrappingInitializer`, which is described in the guide below. 
 
## Why Use Spring On Tanzu GemFire
 
Tanzu GemFire supports the deployment of functions that can be run server-side. One use case for this is that running
code on the server can be very beneficial to performance, especially if you have a lot of data stored in Tanzu GemFire. 

Instead of your application retrieving all of the data from Tanzu GemFire and processing it on the client side, you can
process it on the server side and send back the results. Sending only the result greatly reduces the amount of data sent
over the wire, thereby reducing the time it takes to get your result. 

## Prerequisites

* Basic understanding of Tanzu GemFire including the GemFireSHell (gfsh)
* Know how to use Spring by itself
* Know how to build a jar file

## What You Will Need
 
To make use of this guide you will need:
 
* An IDE or text editor
* JDK 8+
* The example project
* Access to a Tanzu GemFire cluster through the GemFire shell (gfsh)
* A region named “Numbers” containing values of type java.lang.Long. To create and populate this region, run the “setup.gfsh”
script found in the example project.
* Your server-side function code must be written using 
* GemFire 9.9.x/Geode 1.11.x and below: Spring version 4.3.x or Spring Boot 1.5.x

## Guide
 
Using Spring in a server-side function in Tanzu GemFire is possible, but certain aspects may seem unintuitive and there
are limitations.

### A Note About Spring Dependencies
Tanzu GemFire uses Spring on the server and its dependencies are visible to your server-side code.  This means that your
functions Spring version must match the Tanzu GemFire Spring version, which is currently 4.3.23. 

You may have conflicts involving more than just Spring; logging implementations or any other dependencies you use may
conflict with those provided by Tanzu GemFire. To solve this issue, you should develop against the same version of Spring
that Tanzu GemFire uses and do not bundle your Spring dependencies in your jar; this will allow your application to pick
up and use the Spring version already on Tanzu GemFire’s classpath. 

In general, it is easier to bundle as few dependencies as possible; you should only bundle dependencies that are not already
on the Tanzu GemFire classpath. 

You can see what is on the Tanzu GemFire classpath using the gfsh command: 
`gfsh> status server --name=XYZ`
where XYZ is replaced with your server’s name.
 
### Recommended Way to Start a Spring ApplicationContext
 
The simplest way to create a Spring `ApplicationContext` and inject beans on a Tanzu GemFire server is by using
`SpringContextBootstrappingInitializer` from Spring Data GemFire. It will allow you to use Autowiring and maintain a single
`ApplicationContext` across multiple calls that can be shared between functions. However, the `ApplicationContext` cannot
be shared between jars. 

To create an ApplicationContext using `SpringContextBootstrappingInitializer`, do the following:
1. Ensure that your function class implements `org.apache.geode.cache.execute.Function` and that all classes that define
beans, including your function class, extend `LazyWiringDeclarableSupport` from Spring Data GemFire. Not extending this
class will result in your beans not being registered with the `ApplicationContext`. Your function class should look something
like this: 
    ```
    public class MyFunction extends LazyWiringDeclarableSupport implements Function {...}
    ```
2. Annotate your configuration class as you want it. This can be your function class or any other.
    ```
    @Configuration
    ...
    ```
3. Inside your function’s execute() method, register your configuration class with `SpringContextBootstrappingInitializer`,
set the bean ClassLoader to the same ClassLoader as your configuration class, and called `init()` on a new instance of
`SpringContextBootstrappingInitializer` with a new `Properties` object. This can be accomplished with the below code:
    ```
    SpringContextBootstrappingInitializer.register(MyConfig.class);
    
    SpringContextBootstrappingInitializer.setBeanClassLoader(MyConfig.class.getClassLoader());
    
    new SpringContextBootstrappingInitializer()
    
    .init(new Properties());
    ```
4. Build your jar, bundling `spring-data-gemfire` inside (this can be done using Maven, Gradle, IntelliJ, and other ways).
Your jar should contain: 
    * `spring-data-gemfire-2.0.0.RELEASE` (Version subject to change in future releases of Tanzu GemFire)
    * Your function code
    * Any other explicit dependencies you require, besides Spring and Geode

5. Push your jar to the server using the following command in gfsh:
    ```
    gfsh> deploy --jar=/path/to/jar/MyFunction.jar
    ```
6. You may now access the Spring `ApplicationContext` with `SpringContextBootstrappingInitializer.getApplicationContext()`
and beans should be created and injected as expected.
. You now have Spring running on the server!

Note that multiple attempts at initializing the `ApplicationContext` will result in an exception:
```
java.lang.IllegalStateException: A Spring ApplicationContext has already been initialized
```
Therefore, it is advisable to have a separate function specifically for initializing the `ApplicationContext`.
 
### Other Ways to Use Spring
 
The above method is the most convenient and least limited way to use Spring on a Tanzu GemFire server. The sections below
highlight alternative ways in which to use Spring on the server side. While these other may methods may fit your use case,
they have one major drawback: Autowiring is not supported.
 
#### Using a Spring Utility
 
The simplest Spring use case is to use some utility (for example: `StringUtils` or `SpringUtils`) class from Spring that
does not require an `ApplicationContext`. This can be done easily as long as the utility you want is part of a dependency
on Tanzu GemFire’s classpath. All you have to do is use the utility as you normally would, and when building your jar, do
not embed dependencies already on the classpath of the Tanzu GemFire server. If the utility you want to use comes from a
Spring library not present on Tanzu GemFire, such as `SpringUtils` from the Spring Data GemFire project, you may still use
it, but you will need to pack the necessary dependency into your jar.
 
#### Starting a Spring ApplicationContext
 
A more interesting use case is to create an `ApplicationContext` and define beans. To do this, annotate your function
class (or whatever class you want to act as your configuration) with @Configuration and create your context in the `execute()`
method, passing in your configuration class. If you have beans defined outside of your configuration class, you may need
to enable `@ComponentScan` and point it at the package(s) containing your bean definitions. 

Due to how class loading works inside Tanzu GemFire, you may get an error saying that your configuration class cannot be
found. To solve this issue, override `getClassLoader()` on the `ApplicationContext` and return the ClassLoader used to
load your configuration class, like this:
```
context = new AnnotationConfigApplicationContext(MyConfiguration.class) {
   @Override
   public ClassLoader getClassLoader() {
  	return MyConfiguration.class.getClassLoader();
   }
};
```

Now you can use your `ApplicationContext`! 

Unfortunately, injecting beans using `@Autowired` or `@Resource` doesn’t work; you will have to access beans directly
from the context by doing something like `context.getBean(“BeanName”)`. Beans are still injected into other, dependent,
beans. If you want to use Autowiring, use the recommended method described earlier in this guide.
 
#### Using Spring Boot
 
You may wish to use Spring Boot, which is possible. First, you will have to use a version of Spring Boot that is compatible
with the version of Spring that is on the Tanzu GemFire classpath - currently this means using Spring Boot 1.5.x. 
1. Annotate your function class (or whatever class you want to hold your configuration) with `@SpringBootApplication`,
2. Then create your context using `SpringApplication` or `SpringApplicationBuilder` and pass in your configuration class; 

Now you have an `ApplicationContext` without having to mess with ClassLoaders. Because Tanzu GemFire does not have Spring
Boot on the classpath, you will have to pack it into your jar. 

When executing the function, you may get an error like:
```
No auto configuration classes found in META-INF/spring.factories. If you are using a custom packaging, make sure that file is correct
```
The solution may seem counter-intuitive: Remove spring-boot-autoconfigure from your jar. 

Your jar should contain:
* `spring-boot-1.5.20.RELEASE `(Version subject to change in future releases of Tanzu GemFire)
* Your function code
* Any other explicit dependencies you require, besides Spring and Geode.

#### Persisting an ApplicationContext Across Function Calls and Sharing it Between Functions
 
The best way to do this is using the recommended method described above, of starting an ApplicationContext. If you want
to store the ApplicationContext yourself, you can achieve this by storing your context in a static variable either in your
function class or in a separate holder class.
 
#### Sharing an ApplicationContext Between Jars
 
Because of how class loading works in Tanzu GemFire, you cannot share a single `ApplicationContext` between multiple jars
reliably. The jars will see different instances of the `ApplicationContext`. Attempting to share beans between multiple
jars may appear to work under some circumstances, however this may introduce several strange issues that make the results
unpredictable - such as different functions not seeing the same beans in the `ApplicationContext`. Thus, it is recommended
to keep all server-side code that needs to access the same beans in a single jar file.
 
### Limitations
 
* You must use the same version of Spring as Tanzu GemFire has on the classpath. You can see what is on the classpath using
the gfsh command:
```
gfsh> status server --name=XYZ
```
where XYZ is replaced with your server’s name.

* Multiple jars cannot properly share a single `ApplicationContext`.
* Autowiring only works when using the recommended method involving `SpringContextBootstrappingInitializer`.
* You cannot inject Tanzu GemFire objects (the cache, regions, etc.) as beans.
* You cannot configure Tanzu GemFire with Spring Data GemFire annotations.

 
### Problems You May Encounter
 
Objective: Use a Spring utility

Problem: Not including dependencies in your jar causes 
```
Exception: java.lang.NoClassDefFoundError:org/springframework/data/gemfire/util/SpringUtils
and including dependencies causes logging conflicts.
```

Solution: Bundle the dependency that includes the Spring utility, but do not bundle other Spring or Geode dependencies.

Objective: Define and consume a Bean

Problem: Null Pointer Exception when accessing an Autowired value.

Solution: Use the method with `SpringContextBootstrappingInitializer`.

Objective: Use Spring Boot

Problem: 
```
No auto configuration classes found in META-INF/spring.factories.
If you are using a custom packaging, make sure that file is correct
```

Solution: Build your Spring Boot dependency into your jar, but do not include `spring-boot-autoconfigure`.

Objective: Persisting and Sharing an `ApplicationContext` using a custom holder class.

Problem: The configuration class can’t be found when creating the `ApplicationContext`.

Solution: Override the `getClassLoader()` method on `ApplicationContext` and make it return the classloader from your
configuration class.

Objective: Remove, rename, or modify a bean definition and have the change reflected in a shared `ApplicationContext`.

Problem: Different functions see different instances of the `ApplicationContext` with an inconsistent view of defined beans
or you get an error like
```
BeanCreationException: Error creating bean with name 'x' defined in class path resource [com/function/SpringConfig.class]:
No matching factory method found: factory bean SpringConfig; factory method 'getx()'.
Check that a method with the specified name exists and that it is non-static.
```
Solution: Do not try to share an `ApplicationContext` between multiple jars; keep all your bean definitions, bean usages,
and your holder in the same jar.

Objective: Use spring-data-gemfire in our jar

Problem:  Unfortunately, the version of `spring-data-gemfire` that pulls in Spring 4.3 is incompatible with current versions
of Tanzu GemFire, as such, using it will yield the following exception: 
`java.lang.NoClassDefFoundError: com/gemstone/gemfire/cache/Declarable`

Solution: To resolve this, you will need to use a newer version of `spring-data-gemfire`; at the time of writing this,
using `spring-data-gemfire 2.0.0.RELEASE` works. This may change in future releases of Tanzu GemFire. 
