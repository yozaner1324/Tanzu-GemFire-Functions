package com.extra;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.data.gemfire.function.annotation.RegionData;
import org.springframework.data.gemfire.support.LazyWiringDeclarableSupport;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class Function2 extends LazyWiringDeclarableSupport implements Function<String> {

	@Autowired
	@Qualifier("Greeting")
	private String greeting;

	@Resource(name = "Addressee")
	private String addressee;

	//@GemfireFunction(id = "fun2", hasResult = true)
	public void execute(FunctionContext context) {

		Long sum = 0L;
		for (Object i : context.getCache().getRegion("Numbers").values()) {
			sum += (Long) i;
		}

		context.getResultSender().lastResult(greeting + ", " + addressee + "! The sum of all values in /Numbers is " + sum);
	}

	@Override
	public String getId() {
		return "fun2";
	}
}
