/*
 * Copyright 2016 the original author or authors.
 *
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
 */

package capital.scalable.restdocs.jackson.payload;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import capital.scalable.restdocs.jackson.javadoc.JavadocReader;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.validator.constraints.NotBlank;
import org.junit.Test;
import org.springframework.restdocs.AbstractSnippetTests;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.web.method.HandlerMethod;

public class JacksonResponseFieldSnippetTest extends AbstractSnippetTests {

    public JacksonResponseFieldSnippetTest(String name, TemplateFormat templateFormat) {
        super(name, templateFormat);
    }

    @Test
    public void simpleResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));

        HandlerMethod handlerMethod = new HandlerMethod(new TestResource(), "getItem");
        JavadocReader javadocReader = mock(JavadocReader.class);
        when(javadocReader.resolveFieldComment(Item.class, "field1"))
                .thenReturn("A string");
        when(javadocReader.resolveFieldComment(Item.class, "field2"))
                .thenReturn("An integer");

        this.snippet.expectResponseFields("response").withContents(
                tableWithHeader("Path", "Type", "Optional", "Description")
                        .row("field1", "String", "false", "A string")
                        .row("field2", "Integer", "true", "An integer"));

        new JacksonResponseFieldSnippet().document(operationBuilder("response")
                .attribute(HandlerMethod.class.getName(), handlerMethod)
                .attribute(ObjectMapper.class.getName(), mapper)
                .attribute(JavadocReader.class.getName(), javadocReader)
                .request("http://localhost")
                .build());
    }

    @Test
    public void noResponseBody() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));

        HandlerMethod handlerMethod = new HandlerMethod(new TestResource(), "noItem");

        this.snippet.expectResponseFields("response").withContents(
                equalTo("No response body."));

        new JacksonResponseFieldSnippet().document(operationBuilder("response")
                .attribute(HandlerMethod.class.getName(), handlerMethod)
                .attribute(ObjectMapper.class.getName(), mapper)
                .request("http://localhost")
                .build());
    }

    private static class TestResource {

        public Item getItem() {
            return new Item("test");
        }

        public void noItem() {
        }
    }

    private static class Item {
        @NotBlank
        private String field1;
        private Integer field2;

        public Item(String field1) {
            this.field1 = field1;
        }
    }
}