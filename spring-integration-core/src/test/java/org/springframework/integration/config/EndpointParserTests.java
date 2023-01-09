/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.config;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Fisher
 * @author Gary Russell
 * @author Artem Bilan
 */
public class EndpointParserTests {

	@Test
	public void testSimpleEndpoint() throws InterruptedException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"simpleEndpointTests.xml", this.getClass());
		context.start();
		MessageChannel channel = (MessageChannel) context.getBean("endpointParserTestInput");
		TestHandler handler = (TestHandler) context.getBean("testHandler");
		assertThat(handler.getMessageString()).isNull();
		channel.send(new GenericMessage<>("test"));
		assertThat(handler.getLatch().await(10000, TimeUnit.MILLISECONDS)).isTrue();
		assertThat(handler.getMessageString()).isEqualTo("test");
		context.close();
	}

}
