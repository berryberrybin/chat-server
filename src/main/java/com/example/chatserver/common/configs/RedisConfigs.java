package com.example.chatserver.common.configs;

import com.example.chatserver.chat.service.RedisPubSubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfigs {
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;

    // 연결 기본 객체
    @Bean
    @Qualifier("chatPubSub")
    // @Queryfier를 사용하여 다른 RedisConnectionFactory와 구분
    // 예를들어 configuration.setDatabase(0); 으로 설정하고, Quelifier("Login")으로 설정시, 0번 DB는 로그인으로 사용하겠다
    // configuration.setDatabase(1); 으로 설정하고, Quelifier("Cashing")으로 설정시, 1번 DB는 캐싱목적으로 사용하겠다는 의미
    // 현재는 연결 객체를 1개만 사용하기 때문에 database 설정은 하지 않음
    public RedisConnectionFactory chatPubSubFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHost);
        configuration.setPort(redisPort);
        // redis pub/sub에서는 특정 데이터베이스에 의존적이지 않음 (전역적으로 사용하는 기능으로 database 설정은 필요 없음)
        // configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }


    // publish 객체
    // 일반적으로는 StringRedisTemplate을 사용하지 않고, RedisTemplate을 사용함
    // RedisTemplate<key데이터타입, value데이터타입> 형태로 사용함
    // 현재 메시지 전파 목적으로 key-value 형태로 사용하기 때문에 StringRedisTemplate을 사용함
    @Bean
    @Qualifier("chatPubSub") // 여기서도 여러개의 StringRedisTemplate이 있을 수 있기 때문에 @Qualifier를 사용하여 구분
    public StringRedisTemplate stringRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {
    // RedisConnectionFactory을 주입받는데 여러개의 RedisConnectionFactory가 있을 수 있기 때문에 @Qualifier를 사용하여 구분할 수 있음
        return new StringRedisTemplate(redisConnectionFactory);
    }

    // subscribe 객체
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter
            ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        // 메시지를 리슨했다면,이 메시지를 어떻게 할지 처리 방법 필요
        // 즉, 메시지를 처리할 리스너를 추가해야 함 ("XX리스너" 객체에게 메시지 처리 위임)
        container.addMessageListener(messageListenerAdapter, new PatternTopic("chat"));
        // redis pub/sub에서 메시지를 수신할 때, 특정한 토픽("chat")만 발행/구독하도록 설정
        return container;
    }

    // Redis에서 수신된 메시지를 처리할 리스너 구현 (메시지 어떻게 처리할건지)
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService) {
        // RedisPubSubService는 메시지를 수신받아 처리하는 서비스를 구현한 클래스
        // RedisPubSubService의 특정 메서드(메서드명: onMessage)가 수신된 메시지를 처리할 수 있도록 지정
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }

}
