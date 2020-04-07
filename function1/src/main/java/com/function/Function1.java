package com.function;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.data.gemfire.support.LazyWiringDeclarableSupport;
import org.springframework.data.gemfire.support.SpringContextBootstrappingInitializer;
import org.springframework.stereotype.Component;

import java.util.Properties;

//@Component
public class Function1 extends LazyWiringDeclarableSupport implements Function<String> {

	//@GemfireFunction(id = "fun1")
	public void execute(FunctionContext context) {

		SpringContextBootstrappingInitializer.register(SpringConfig.class);
		SpringContextBootstrappingInitializer.setBeanClassLoader(SpringConfig.class.getClassLoader());
		new SpringContextBootstrappingInitializer().init(new Properties());

		context.getResultSender().lastResult("ApplicationContext Created");
	}

	@Override
	public String getId() {
		return "fun1";
	}
}
