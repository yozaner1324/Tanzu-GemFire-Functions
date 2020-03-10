package com.function;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.springframework.data.gemfire.support.LazyWiringDeclarableSupport;
import org.springframework.data.gemfire.support.SpringContextBootstrappingInitializer;

import java.util.Properties;

public class Function1 extends LazyWiringDeclarableSupport implements Function {

	@Override
	public void execute(FunctionContext functionContext) {

		SpringContextBootstrappingInitializer.register(SpringConfig.class);
		SpringContextBootstrappingInitializer.setBeanClassLoader(SpringConfig.class.getClassLoader());
		new SpringContextBootstrappingInitializer().init(new Properties());

		functionContext.getResultSender().lastResult("ApplicationContext Created.");
	}

	public String getId() {
		return "fun1";
	}
}
