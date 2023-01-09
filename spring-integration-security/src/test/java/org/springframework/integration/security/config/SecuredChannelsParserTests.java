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

package org.springframework.integration.security.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.integration.channel.AbstractPollableChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.security.channel.ChannelAccessPolicy;
import org.springframework.integration.security.channel.ChannelSecurityInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jonas Partner
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 */
@ContextConfiguration
public class SecuredChannelsParserTests extends AbstractJUnit4SpringContextTests {

	TestMessageChannel messageChannel;

	@Before
	public void setUp() {
		messageChannel = new TestMessageChannel();
	}

	@Test
	public void testAdminRequiredForSend() {
		String beanName = "adminRequiredForSend";
		messageChannel.setBeanName(beanName);
		MessageChannel proxy = (MessageChannel) applicationContext.getAutowireCapableBeanFactory()
				.applyBeanPostProcessorsAfterInitialization(messageChannel, beanName);
		assertThat(AopUtils.isAopProxy(proxy)).as("Channel was not proxied").isTrue();
		Advisor[] advisors = ((Advised) proxy).getAdvisors();
		assertThat(advisors.length).as("Wrong number of interceptors").isEqualTo(1);
		ChannelSecurityInterceptor interceptor = (ChannelSecurityInterceptor) advisors[0].getAdvice();
		ChannelAccessPolicy policy = this.retrievePolicyForPatternString(beanName, interceptor);
		assertThat(policy).as("Pattern '" + beanName + "' is not included in mappings").isNotNull();
		Collection<ConfigAttribute> sendDefinition = policy.getConfigAttributesForSend();
		Collection<ConfigAttribute> receiveDefinition = policy.getConfigAttributesForReceive();
		assertThat(this.getRolesFromDefintion(sendDefinition).contains("ROLE_ADMIN"))
				.as("ROLE_ADMIN not found as send attribute").isTrue();
		assertThat(receiveDefinition.size() == 0).as("Policy applies to receive").isTrue();
	}

	@Test
	public void testAdminOrUserRequiredForSend() {
		String beanName = "adminOrUserRequiredForSend";
		messageChannel.setBeanName(beanName);
		MessageChannel proxy = (MessageChannel) applicationContext.getAutowireCapableBeanFactory()
				.applyBeanPostProcessorsAfterInitialization(messageChannel, beanName);
		assertThat(AopUtils.isAopProxy(proxy)).as("Channel was not proxied").isTrue();
		Advisor[] advisors = ((Advised) proxy).getAdvisors();
		assertThat(advisors.length).as("Wrong number of interceptors").isEqualTo(1);
		ChannelSecurityInterceptor interceptor = (ChannelSecurityInterceptor) advisors[0].getAdvice();
		ChannelAccessPolicy policy = this.retrievePolicyForPatternString(beanName, interceptor);
		assertThat(policy).as("Pattern '" + beanName + "' is not included in mappings").isNotNull();
		Collection<ConfigAttribute> sendDefinition = policy.getConfigAttributesForSend();
		Collection<ConfigAttribute> receiveDefinition = policy.getConfigAttributesForReceive();
		Collection<String> sendRoles = this.getRolesFromDefintion(sendDefinition);
		assertThat(sendRoles.contains("ROLE_ADMIN")).as("ROLE_ADMIN not found as send attribute").isTrue();
		assertThat(sendRoles.contains("ROLE_USER")).as("ROLE_USER not found as send attribute").isTrue();
		assertThat(receiveDefinition.size() == 0).as("Policy applies to receive").isTrue();
	}

	@Test
	public void testAdminRequiredForReceive() {
		String beanName = "adminRequiredForReceive";
		messageChannel.setBeanName(beanName);
		MessageChannel proxy = (MessageChannel) applicationContext.getAutowireCapableBeanFactory()
				.applyBeanPostProcessorsAfterInitialization(messageChannel, beanName);
		assertThat(AopUtils.isAopProxy(proxy)).as("Channel was not proxied").isTrue();
		Advisor[] advisors = ((Advised) proxy).getAdvisors();
		assertThat(advisors.length).as("Wrong number of interceptors").isEqualTo(1);
		ChannelSecurityInterceptor interceptor = (ChannelSecurityInterceptor) advisors[0].getAdvice();
		ChannelAccessPolicy policy = this.retrievePolicyForPatternString(beanName, interceptor);
		assertThat(policy).as("Pattern '" + beanName + "' is not included in mappings").isNotNull();
		Collection<ConfigAttribute> sendDefinition = policy.getConfigAttributesForSend();
		Collection<ConfigAttribute> receiveDefinition = policy.getConfigAttributesForReceive();
		Collection<String> receiveRoles = this.getRolesFromDefintion(receiveDefinition);
		assertThat(receiveRoles.contains("ROLE_ADMIN")).as("ROLE_ADMIN not found as receive attribute").isTrue();
		assertThat(sendDefinition.size() == 0).as("Policy applies to receive").isTrue();
	}

	@Test
	public void testAdminOrUserRequiredForReceive() {
		String beanName = "adminOrUserRequiredForReceive";
		messageChannel.setBeanName(beanName);
		MessageChannel proxy = (MessageChannel) applicationContext.getAutowireCapableBeanFactory()
				.applyBeanPostProcessorsAfterInitialization(messageChannel, beanName);
		assertThat(AopUtils.isAopProxy(proxy)).as("Channel was not proxied").isTrue();
		Advisor[] advisors = ((Advised) proxy).getAdvisors();
		assertThat(advisors.length).as("Wrong number of interceptors").isEqualTo(1);
		ChannelSecurityInterceptor interceptor = (ChannelSecurityInterceptor) advisors[0].getAdvice();
		ChannelAccessPolicy policy = this.retrievePolicyForPatternString(beanName, interceptor);
		assertThat(policy).as("Pattern '" + beanName + "' is not included in mappings").isNotNull();
		Collection<ConfigAttribute> sendDefinition = policy.getConfigAttributesForSend();
		Collection<ConfigAttribute> receiveDefinition = policy.getConfigAttributesForReceive();
		Collection<String> receiveRoles = this.getRolesFromDefintion(receiveDefinition);
		assertThat(receiveRoles.contains("ROLE_ADMIN")).as("ROLE_ADMIN not found as receive attribute").isTrue();
		assertThat(receiveRoles.contains("ROLE_USER")).as("ROLE_USER not found as receive attribute").isTrue();
		assertThat(sendDefinition.size() == 0).as("Policy applies to receive").isTrue();
	}

	@Test
	public void testAdminRequiredForSendAndReceive() {
		String beanName = "adminRequiredForSendAndReceive";
		messageChannel.setBeanName(beanName);
		MessageChannel proxy = (MessageChannel) applicationContext.getAutowireCapableBeanFactory()
				.applyBeanPostProcessorsAfterInitialization(messageChannel, beanName);
		assertThat(AopUtils.isAopProxy(proxy)).as("Channel was not proxied").isTrue();
		Advisor[] advisors = ((Advised) proxy).getAdvisors();
		assertThat(advisors.length).as("Wrong number of interceptors").isEqualTo(1);
		ChannelSecurityInterceptor interceptor = (ChannelSecurityInterceptor) advisors[0].getAdvice();
		ChannelAccessPolicy policy = this.retrievePolicyForPatternString(beanName, interceptor);
		assertThat(policy).as("Pattern '" + beanName + "' is not included in mappings").isNotNull();
		Collection<ConfigAttribute> sendDefinition = policy.getConfigAttributesForSend();
		Collection<ConfigAttribute> receiveDefinition = policy.getConfigAttributesForReceive();
		assertThat(sendDefinition).as("Pattern does not apply to 'send'").isNotNull();
		assertThat(receiveDefinition).as("Pattern does not apply to 'receive'").isNotNull();
		Collection<String> sendRoles = this.getRolesFromDefintion(sendDefinition);
		Collection<String> receiveRoles = this.getRolesFromDefintion(receiveDefinition);
		assertThat(sendRoles.contains("ROLE_ADMIN")).as("ROLE_ADMIN not found in send attributes").isTrue();
		assertThat(receiveRoles.contains("ROLE_ADMIN")).as("ROLE_ADMIN not found in receive attributes").isTrue();
	}


	@SuppressWarnings("unchecked")
	private ChannelAccessPolicy retrievePolicyForPatternString(String patternString, ChannelSecurityInterceptor interceptor) {
		DirectFieldAccessor accessor = new DirectFieldAccessor(interceptor.obtainSecurityMetadataSource());
		Map<Pattern, ChannelAccessPolicy> policies = (Map<Pattern, ChannelAccessPolicy>) accessor.getPropertyValue("patternMappings");
		for (Map.Entry<Pattern, ChannelAccessPolicy> entry : policies.entrySet()) {
			if (entry.getKey().pattern().equals(patternString)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private Collection<String> getRolesFromDefintion(Collection<ConfigAttribute> definition) {
		Set<String> roles = new HashSet<String>();
		//Collection configAttributes = SecurityConfig.createListFromCommaDelimitedString(definition);
		for (ConfigAttribute nextConfigAttribute : definition) {
			ConfigAttribute attribute = nextConfigAttribute;
			roles.add(attribute.getAttribute());
		}
		return roles;
	}


	static class TestMessageChannel extends AbstractPollableChannel {

		List<ChannelInterceptor> interceptors = new ArrayList<ChannelInterceptor>();


		@Override
		protected Message<?> doReceive(long timeout) {
			return null;
		}

		@Override
		protected boolean doSend(Message<?> message, long timeout) {
			return false;
		}

		public List<Message<?>> clear() {
			return null;
		}

		public List<Message<?>> purge(MessageSelector selector) {
			return null;
		}

		@Override
		public void addInterceptor(ChannelInterceptor interceptor) {
			interceptors.add(interceptor);
		}

	}

}
