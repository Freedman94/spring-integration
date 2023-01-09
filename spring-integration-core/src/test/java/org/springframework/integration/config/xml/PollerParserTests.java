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

package org.springframework.integration.config.xml;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.aopalliance.aop.Advice;
import org.junit.Test;

import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.config.TestTrigger;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 * @author Artem Bilan
 */
public class PollerParserTests {

	@Test
	public void defaultPollerWithId() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"defaultPollerWithId.xml", PollerParserTests.class);
		Object poller = context.getBean("defaultPollerWithId");
		assertThat(poller).isNotNull();
		Object defaultPoller = context.getBean(PollerMetadata.DEFAULT_POLLER_METADATA_BEAN_NAME);
		assertThat(defaultPoller).isNotNull();
		assertThat(context.getBean("defaultPollerWithId")).isEqualTo(defaultPoller);
		context.close();
	}

	@Test
	public void defaultPollerWithoutId() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"defaultPollerWithoutId.xml", PollerParserTests.class);
		Object defaultPoller = context.getBean(PollerMetadata.DEFAULT_POLLER_METADATA_BEAN_NAME);
		assertThat(defaultPoller).isNotNull();
		context.close();
	}

	@Test(expected = BeanDefinitionParsingException.class)
	public void multipleDefaultPollers() {
		new ClassPathXmlApplicationContext(
				"multipleDefaultPollers.xml", PollerParserTests.class).close();
	}

	@Test(expected = BeanDefinitionParsingException.class)
	public void topLevelPollerWithoutId() {
		new ClassPathXmlApplicationContext(
				"topLevelPollerWithoutId.xml", PollerParserTests.class).close();
	}

	@Test
	public void pollerWithAdviceChain() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"pollerWithAdviceChain.xml", PollerParserTests.class);
		Object poller = context.getBean("poller");
		assertThat(poller).isNotNull();
		PollerMetadata metadata = (PollerMetadata) poller;
		assertThat(metadata.getAdviceChain()).isNotNull();
		assertThat(metadata.getAdviceChain().size()).isEqualTo(4);
		assertThat(metadata.getAdviceChain().get(0)).isSameAs(context.getBean("adviceBean1"));
		assertThat(metadata.getAdviceChain().get(1).getClass()).isEqualTo(TestAdviceBean.class);
		assertThat(((TestAdviceBean) metadata.getAdviceChain().get(1)).getId()).isEqualTo(2);
		assertThat(metadata.getAdviceChain().get(2)).isSameAs(context.getBean("adviceBean3"));
		Advice txAdvice = metadata.getAdviceChain().get(3);
		assertThat(txAdvice.getClass()).isEqualTo(TransactionInterceptor.class);
		TransactionAttributeSource transactionAttributeSource = ((TransactionInterceptor) txAdvice).getTransactionAttributeSource();
		assertThat(transactionAttributeSource.getClass()).isEqualTo(NameMatchTransactionAttributeSource.class);
		@SuppressWarnings("rawtypes")
		HashMap nameMap = TestUtils.getPropertyValue(transactionAttributeSource, "nameMap", HashMap.class);
		assertThat(nameMap.size()).isEqualTo(1);
		assertThat(nameMap.toString()).isEqualTo("{*=PROPAGATION_REQUIRES_NEW,ISOLATION_DEFAULT,readOnly}");
		context.close();

	}

	@Test
	public void pollerWithReceiveTimeoutAndTimeunit() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"pollerWithReceiveTimeout.xml", PollerParserTests.class);
		Object poller = context.getBean("poller");
		assertThat(poller).isNotNull();
		PollerMetadata metadata = (PollerMetadata) poller;
		assertThat(metadata.getReceiveTimeout()).isEqualTo(1234);
		PeriodicTrigger trigger = (PeriodicTrigger) metadata.getTrigger();
		assertThat(TestUtils.getPropertyValue(trigger, "timeUnit").toString()).isEqualTo(TimeUnit.SECONDS.toString());
		context.close();
	}

	@Test
	public void pollerWithTriggerReference() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"pollerWithTriggerReference.xml", PollerParserTests.class);
		Object poller = context.getBean("poller");
		assertThat(poller).isNotNull();
		PollerMetadata metadata = (PollerMetadata) poller;
		assertThat(metadata.getTrigger() instanceof TestTrigger).isTrue();
		context.close();
	}

	@Test(expected = BeanDefinitionParsingException.class)
	public void pollerWithCronTriggerAndTimeUnit() {
		new ClassPathXmlApplicationContext(
				"cronTriggerWithTimeUnit-fail.xml", PollerParserTests.class).close();
	}

	@Test(expected = BeanDefinitionParsingException.class)
	public void topLevelPollerWithRef() {
		new ClassPathXmlApplicationContext(
				"defaultPollerWithRef.xml", PollerParserTests.class).close();
	}

	@Test(expected = BeanDefinitionParsingException.class)
	public void pollerWithCronAndFixedDelay() {
		new ClassPathXmlApplicationContext(
				"pollerWithCronAndFixedDelay.xml", PollerParserTests.class).close();
	}

}
