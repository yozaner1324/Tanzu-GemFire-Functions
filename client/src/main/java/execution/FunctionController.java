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

package execution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FunctionController {

    @Autowired
    FunctionExecutions functionExecutions;

    @RequestMapping(value="/report", produces = "text/html")
    public String getReport() {

        // in case the ApplicationContext has already been initialized
        try {
            functionExecutions.createContext();
        } catch(Exception e){ }

        String report = functionExecutions.getServerReport().get(0);

        return "<html><body>"
                + report + "<br>"
                + "</body></html>";
    }
}
