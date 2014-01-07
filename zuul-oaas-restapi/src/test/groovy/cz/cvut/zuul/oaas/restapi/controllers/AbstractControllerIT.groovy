/*
 * The MIT License
 *
 * Copyright 2013-2014 Czech Technical University in Prague.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.cvut.zuul.oaas.restapi.controllers

import cz.cvut.zuul.oaas.api.test.ApiObjectFactory
import cz.cvut.zuul.oaas.restapi.controllers.CommonExceptionHandler
import cz.cvut.zuul.oaas.restapi.test.AdvicedStandaloneMockMvcBuilder
import groovy.json.JsonSlurper
import org.codehaus.jackson.map.ObjectMapper
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.web.bind.annotation.RequestMapping
import spock.lang.Shared
import spock.lang.Specification

import static org.codehaus.jackson.map.PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES
import static org.springframework.http.MediaType.APPLICATION_JSON

abstract class AbstractControllerIT extends Specification {

    protected static final CONTENT_TYPE_JSON = "application/json;charset=UTF-8"

    @Delegate
    static ApiObjectFactory factory = new ApiObjectFactory()

    @Shared MockMvc mockMvc
    @Shared controller
    ResponseWrapper response

    def baseUri

    static GET = MockMvcRequestBuilders.&get
    static POST = MockMvcRequestBuilders.&post
    static PUT = MockMvcRequestBuilders.&put
    static DELETE = MockMvcRequestBuilders.&delete


    abstract def initController()
    void setupController(_) {}


    void setupSpec() {
        def messageConverter = new MappingJacksonHttpMessageConverter(
                objectMapper: new ObjectMapper(
                    propertyNamingStrategy: CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES
                )
        )
        controller = initController()
        mockMvc = new AdvicedStandaloneMockMvcBuilder(controller)
                .setControllerAdvices(new CommonExceptionHandler())
                .setMessageConverters(messageConverter)
                .defaultRequest(GET('/')
                    .accept( APPLICATION_JSON )
                    .contentType( APPLICATION_JSON) )
                .build()
    }

    def setup() {
        setupController(controller)

        baseUri = getBaseUri() ?: determineBaseUri()
        assert baseUri, 'baseUri was not found, must be specified explicitly'
    }


    def perform(MockHttpServletRequestBuilder requestBuilder) {
        requestBuilder.with(new RequestPostProcessor() {
            MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.requestURI = "${getBaseUri()}/${request.requestURI}"
                return request
            }
        })
        def result = mockMvc.perform(requestBuilder).andReturn()
        response = new ResponseWrapper(result.getResponse())
    }

    private determineBaseUri() {
        def requestMapping = controller.class.getAnnotation(RequestMapping)
        if (requestMapping) {
            def uri = requestMapping.value()[0]
            return uri.endsWith('/') ? uri[0..-2] : uri
        }
    }


    static class ResponseWrapper {

        @Delegate
        private final MockHttpServletResponse response

        @Lazy json = new JsonSlurper().parseText(contentAsString)

        ResponseWrapper(MockHttpServletResponse response) {
            this.response = response
        }
    }
}
