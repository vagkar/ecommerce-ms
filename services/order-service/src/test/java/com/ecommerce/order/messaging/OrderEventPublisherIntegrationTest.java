package com.ecommerce.order.messaging;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.client.ProductView;
import com.ecommerce.order.dto.CreateOrderItemRequest;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.service.OrderService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"order.events"}
)
@TestPropertySource(properties = {
        // Point the app to the embedded broker (overrides application.yml)
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        // Use an in-memory H2 database instead of PostgreSQL
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class OrderEventPublisherIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    // Mock ProductClient — we don't want real HTTP calls in integration tests
    @MockitoBean
    private ProductClient productClient;

    @Test
    void createOrder_publishesOrderCreatedEventToKafka() throws InterruptedException {
        // Arrange: set up a consumer to listen to order.events topic
        var consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        consumerProps.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreatedEvent.class.getName());
        consumerProps.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.ecommerce");

        var consumerFactory = new DefaultKafkaConsumerFactory<String, OrderCreatedEvent>(consumerProps);
        var containerProps = new ContainerProperties("order.events");

        // Queue to collect received messages
        BlockingQueue<ConsumerRecord<String, OrderCreatedEvent>> records = new LinkedBlockingQueue<>();
        containerProps.setMessageListener((MessageListener<String, OrderCreatedEvent>) records::add);

        var container = new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());

        // Arrange: prepare test data
        var userId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        when(productClient.getProduct(productId))
                .thenReturn(new ProductView(productId, "Laptop", new BigDecimal("999.99"), true));

        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(productId, 2)
        ));

        // Act: create order — this should publish an event to Kafka
        var order = orderService.create(request, userId);

        // Assert: wait up to 5 seconds for the event to arrive
        var record = records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();

        var event = record.value();
        assertThat(event.orderId()).isEqualTo(order.getId());
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.total()).isEqualByComparingTo(new BigDecimal("1999.98"));

        container.stop();
    }
}