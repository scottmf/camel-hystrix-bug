/**
 * Copyright (C) 2018 Scott Feldstein
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.scottieknows.disruptor;

import static java.lang.String.*;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;

@SpringBootApplication
public class CamelDisruptor {

    public static final String URI = "disruptor:input?multipleConsumers=true&concurrentConsumers=3";

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(CamelDisruptor.class, args);
    }

    @Bean
    public RouteBuilder routeBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(URI)
                    .errorHandler(deadLetterChannel(format("log:%s?level=ERROR", CamelDisruptor.class.getName())))
                    .onException(Throwable.class)
//                    .onException(RuntimeException.class)
//                    .onException(CamelExecutionException.class)
                        .maximumRedeliveries(5).redeliveryDelay("100")
//                        .to("log:error?showCaughtException=true&showStackTrace=true")
                        .to("log:error?showCaughtException=true")
                    .end()
                    .to("direct:first")
                    .hystrix()
                        .hystrixConfiguration()
                            .circuitBreakerEnabled(false)
                            .circuitBreakerErrorThresholdPercentage(90)
                            .circuitBreakerRequestVolumeThreshold(100000)
                            .circuitBreakerSleepWindowInMilliseconds(1)
                            .executionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE.toString())
                            .executionTimeoutInMilliseconds(5000)
                            .requestLogEnabled(true)
                            .fallbackEnabled(true)
                        .end()
                        .to("direct:second")
//                        .onFallback().to("log:failure?level=ERROR&showCaughtException=true&showStackTrace=true")
//                        .onFallback().to("log:failure?level=ERROR&showCaughtException=true")
                    .endHystrix()
                    .to("direct:third")
                    .to("log:success?level=INFO");
            }
        };
    }
}
