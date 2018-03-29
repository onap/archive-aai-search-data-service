/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.sa;

// import org.eclipse.jetty.util.security.Password;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

//	public static String[] deobfuscateArgs(String[] args, String ... attrnames) {
//
//		String[] deobfuscatedArgs = args.clone();
//
//		Password.deobfuscate("HI");
//
//		//System.setProperty(arg0, arg1)
//
//		return deobfuscatedArgs;
//	}
//
    public static void main(String[] args) {
    	
    	//server.ssl.key-store-password=onapSecret
    	//server.ssl.key-password=onapSecret
//    	args = new String[]{"-Dserver.ssl.key-store-password", "onapSecret",
//    			"-Dserver.ssl.key-password", "onapSecret"};
    	
    	SpringApplication.run(Application.class, args);

    	//deobfuscateArgs(args, "server.ssl.key-store-password", "server.ssl.key-password"));
    }
}
