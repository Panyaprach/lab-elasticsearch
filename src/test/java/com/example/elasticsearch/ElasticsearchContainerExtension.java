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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ElasticsearchContainerExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor {
    protected static final String ELASTICSEARCH_DEFAULT_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch";
    protected static final String ELASTICSEARCH_DEFAULT_VERSION = "7.9.2";
    protected ElasticsearchContainer container;
    protected RestClientBuilder builder;

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        container.close();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        final String image = ELASTICSEARCH_DEFAULT_IMAGE + ":" + ELASTICSEARCH_DEFAULT_VERSION;
        container = new ElasticsearchContainer(image);
        // Start the container. This step might take some time...
        container.start();

        HttpHost httpHost = HttpHost.create(container.getHttpHostAddress());
        builder = RestClient.builder(httpHost);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        List<Field> fields = Arrays.stream(testClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(TestRestClient.class))
                .collect(Collectors.toList());

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType().equals(RestHighLevelClient.class)) {
                field.set(testInstance, new RestHighLevelClient(builder));
            }
        }
    }
}
