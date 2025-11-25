package kuke.board.hotarticle.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class KafkaConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public NewTopic kukeBoardArticleTopic() {
        return TopicBuilder.name("kuke-board-article")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic kukeBoardCommentTopic() {
        return TopicBuilder.name("kuke-board-comment")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic kukeBoardLikeTopic() {
        return TopicBuilder.name("kuke-board-like")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic kukeBoardViewTopic() {
        return TopicBuilder.name("kuke-board-view")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
