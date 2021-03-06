/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.dcae;

import org.onap.sdc.dcae.controller.proxy.DcaeProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.*;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


@Configuration
@ComponentScan()
@EnableAutoConfiguration
@PropertySource("file:${jetty.base}/config/dcae-fe/application.properties")
public class FeApp extends SpringBootServletInitializer implements CommandLineRunner{

	private static final String SPECIFICATION_VERSION = "Specification-Version";
	@Autowired
	ServletContext servletContext;

	private static final String MANIFEST_FILE_NAME = "/META-INF/MANIFEST.MF";
	private static String dcaeVersion;

    @Value("${beUrl}")
	private String beUrl;

    public static void main(String[] args) {
        SpringApplication.run(FeApp.class, args);
    }

	public void run(String... arg0) throws Exception {
		InputStream inputStream = servletContext.getResourceAsStream(MANIFEST_FILE_NAME);

		System.out.println("Server is starting..reading DCAE version...");

		String version = null;
		try {
			Manifest mf = new Manifest(inputStream);
			Attributes atts = mf.getMainAttributes();
			version = atts.getValue(SPECIFICATION_VERSION);
			if (version == null || version.isEmpty()) {
				System.err.println("failed to read DCAE version from MANIFEST.");
			} else {
				System.out.println("DCAE version from MANIFEST is "+ version);
				dcaeVersion = version;
			}

		} catch (IOException e) {
			System.err.println("failed to read DCAE version from MANIFEST: "+ e.getMessage());
		}
	}

	public static String getDcaeVersion() {
		return dcaeVersion;
	}


    @Bean
    public ServletRegistrationBean dcaeProxyBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new DcaeProxy(beUrl), "/dcaeProxy/*");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
