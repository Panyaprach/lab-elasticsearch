package com.example.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Testcontainers
public class ElasticsearchContainerExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor {
    protected static final String ELASTICSEARCH_DEFAULT_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch";
    protected static final String ELASTICSEARCH_DEFAULT_VERSION = "7.9.2";
    @Container
    protected ElasticsearchContainer container;
    protected RestHighLevelClient client;

    protected boolean requireInjection(Field field) {
        return field.isAnnotationPresent(TestRestHighLevelClient.class);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        client.close();
        container.close();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        final String image = ELASTICSEARCH_DEFAULT_IMAGE + ":" + ELASTICSEARCH_DEFAULT_VERSION;
        container = new ElasticsearchContainer(image);
        // Start the container. This step might take some time...
        container.start();

        String containerAddress = container.getHttpHostAddress();
        HttpHost httpHost = HttpHost.create(containerAddress);
        RestClientBuilder builder = RestClient.builder(httpHost);
        client = new RestHighLevelClient(builder);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        List<Field> fields = Arrays.stream(testClass.getDeclaredFields())
                .filter(this::requireInjection)
                .collect(Collectors.toList());

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType().equals(RestHighLevelClient.class)) {
                field.set(testInstance, client);
            } else {
                throw new UnsupportedOperationException(
                        String.format("%s doesn't supported %s", TestRestHighLevelClient.class, field.getType())
                );
            }
        }
    }
}
