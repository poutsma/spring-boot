/*
 * Copyright 2012-2023 the original author or authors.
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

package org.springframework.boot.autoconfigure.web.client;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link RestClientAutoConfiguration}
 *
 * @author Arjen Poutsma
 */
class RestClientAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class));

	@Test
	void shouldCreateBuilder() {
		this.contextRunner.run((context) -> {
			RestClient.Builder builder = context.getBean(RestClient.Builder.class);
			RestClient restClient = builder.build();
			assertThat(restClient).isNotNull();
		});
	}

	@Test
	void restClientShouldApplyCustomizers() {
		this.contextRunner.withUserConfiguration(RestClientCustomizerConfig.class).run((context) -> {
			RestClient.Builder builder = context.getBean(RestClient.Builder.class);
			RestClientCustomizer customizer = context.getBean("webClientCustomizer", RestClientCustomizer.class);
			builder.build();
			then(customizer).should().customize(any(RestClient.Builder.class));
		});
	}

	@Test
	void shouldGetPrototypeScopedBean() {
		this.contextRunner.withUserConfiguration(RestClientCustomizerConfig.class).run((context) -> {
			RestClient.Builder firstBuilder = context.getBean(RestClient.Builder.class);
			RestClient.Builder secondBuilder = context.getBean(RestClient.Builder.class);
			assertThat(firstBuilder).isNotEqualTo(secondBuilder);
		});
	}

	@Test
	void shouldNotCreateClientBuilderIfAlreadyPresent() {
		this.contextRunner.withUserConfiguration(CustomRestClientBuilderConfig.class).run((context) -> {
			RestClient.Builder builder = context.getBean(RestClient.Builder.class);
			assertThat(builder).isInstanceOf(MyWebClientBuilder.class);
		});
	}

	@Configuration(proxyBeanMethods = false)
	static class CodecConfiguration {

		@Bean
		CodecCustomizer myCodecCustomizer() {
			return mock(CodecCustomizer.class);
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class RestClientCustomizerConfig {

		@Bean
		RestClientCustomizer webClientCustomizer() {
			return mock(RestClientCustomizer.class);
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CustomRestClientBuilderConfig {

		@Bean
		MyWebClientBuilder myWebClientBuilder() {
			return mock(MyWebClientBuilder.class);
		}

	}

	interface MyWebClientBuilder extends RestClient.Builder {

	}

}