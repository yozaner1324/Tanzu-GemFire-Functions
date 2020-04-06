package com.extra;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.gemfire.support.LazyWiringDeclarableSupport;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Function2 extends LazyWiringDeclarableSupport implements Function<String> {

	@Autowired
	@Qualifier("Greeting")
	private String greeting;

	@Resource(name = "Addressee")
	private String addressee;

	@Override
	public void execute(FunctionContext functionContext) {

		if(functionContext.getCache().getRegion("/Numbers") != null) {
			Long sum = 0L;
			for (Object i : functionContext.getCache().getRegion("/Numbers").values()) {
				sum += (Long) i;
			}

			functionContext.getResultSender().lastResult(greeting + ", " + addressee + "! The sum of all values in /Numbers is " + sum);
		} else {
			throw new IllegalStateException("Region /Numbers has not been created. You can create it manually or by running setup.gfsh.");
		}
	}

	@Override
	public String getId() {
		return "fun2";
	}
}
