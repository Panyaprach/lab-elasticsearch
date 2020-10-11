package com.example.elasticsearch;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(OrderAnnotation.class)
@ExtendWith({ElasticsearchContainerExtension.class, TimingExtension.class})
public class ElasticsearchIntegrationTest {

    @TestRestClient
    private RestHighLevelClient client;

    @Test
    @Order(1)
    public void whenCheckClusterHealthStatus_expectGreen() throws Exception {
        ClusterHealthRequest request = new ClusterHealthRequest();
        ClusterHealthResponse response = client.cluster().health(request, RequestOptions.DEFAULT);
        assertEquals(ClusterHealthStatus.GREEN, response.getStatus());
    }

    @Test
    @Order(2)
    public void whenCreateIndex_thenSuccess() throws Exception {
        String expectedIndex = "my-index";
        CreateIndexRequest request = new CreateIndexRequest("my-index");

        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);

        assertTrue(response.isAcknowledged());
        assertEquals(expectedIndex, response.index());
    }

    @Test
    @Order(3)
    public void whenGetIndex_thenSuccess() throws Exception {
        GetIndexRequest request = new GetIndexRequest("my-index");
        GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);

        assertEquals(1, response.getIndices().length);
    }
}
