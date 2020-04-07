/*
Copyright (C) 2020-Present Pivotal Software, Inc. All rights reserved.

This program and the accompanying materials are made available under the terms of the under the Apache License, Version
2.0 (the "License”); you may not use this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.
 */

package com.function;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.springframework.data.gemfire.support.LazyWiringDeclarableSupport;
import org.springframework.data.gemfire.support.SpringContextBootstrappingInitializer;

import java.util.Properties;

public class Function1 extends LazyWiringDeclarableSupport implements Function<String> {

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
