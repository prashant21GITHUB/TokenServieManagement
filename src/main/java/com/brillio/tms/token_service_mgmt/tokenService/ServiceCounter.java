package com.brillio.tms.token_service_mgmt.tokenService;

import com.brillio.tms.token_service_mgmt.enums.TokenCategory;
import com.brillio.tms.token_service_mgmt.kafka.KafkaConsumerService;
import com.brillio.tms.token_service_mgmt.kafka.KafkaMonitorService;
import com.brillio.tms.token_service_mgmt.models.ApplicantTokenRecord;
import com.brillio.tms.token_service_mgmt.models.Token;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class serves the generated token requests. Each instance of this class is associated with a dedicated
 * token category. See {@link TokenCategory}
 *
 * It subscribe to a kafka topic defined in {@literal service.counter.queue.names} setting in application.properties
 * Below settings works in conjunction:
 *      {@literal service.counter.queue.names}
 * and  {@literal service.counter.id.category.pairs}
 *
 * Example:
 *     service.counter.id.category.pairs : SC1:NORMAL,SC2:NORMAL,SC3:PREMIUM,SC4:PREMIUM
 *     service.counter.queue.names : SC1,SC2,SC3,SC4
 *
 * Now for each generated token, based on its category that token will be assigned to one of its suitable service counter.
 * TokenGenerationService will publish the token to its assigned service counter's queue name(kafka topic) and then
 * the responsible service counter will consume that token and serve it.
 *
 *     Example:
 *         If generated token is :  {Token {tokenNumber:6, tokenCategory:PREMIUM}, ServiceCounter:SC7 }
 *      Then this message will be published on kafka topic "SC7" and the corresponding service counter which already
 *      has subscribed to "SC7" will consume this message at some point in future and serve it.
 *
 */
public class ServiceCounter implements IServiceCounter {

    private final BlockingQueue<Token> tokensQueue;
    private final static int MAX_REQUESTS = 500;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final TokenCategory category;
    private ExecutorService executorService;
    //Kafka topic
    private final String queueName;
    private final String counterName;
    private final Consumer<String, ApplicantTokenRecord> kafkaConsumer;
    private final AtomicBoolean isSubscribedToTopic = new AtomicBoolean(false);
    private final KafkaMonitorService kafkaMonitorService;
    private final AtomicBoolean isKafkaServiceRunning = new AtomicBoolean(false);
    private ExecutorService waitingThread;
    private final Logger LOGGER = LoggerFactory.getLogger("ServiceCounter");

    public ServiceCounter(TokenCategory category,
                          String counterName,
                          String queueName,
                          KafkaConsumerService kafkaConsumerService, KafkaMonitorService kafkaMonitorService) {
        this.queueName = queueName;
        this.counterName = counterName;
        this.kafkaMonitorService = kafkaMonitorService;
        this.tokensQueue = new LinkedBlockingQueue<>(MAX_REQUESTS);
        this.category = category;
        this.kafkaConsumer = kafkaConsumerService.newConsumer();
        this.kafkaMonitorService.startMonitoring(isRunning -> isKafkaServiceRunning.set(isRunning));
        this.waitingThread = Executors.newFixedThreadPool(1);
    }

    private void serveToken(Token token) {
        try {
            tokensQueue.put(token);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startCounter() {
        if(!isStarted.getAndSet(true)) {
            executorService = Executors.newFixedThreadPool(2, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("ServiceCounter_" + ServiceCounter.this.counterName);
                    return t;
                }
            });

            executorService.submit(() -> {
               while (isStarted.get()) {
                   try {
                       Token token = tokensQueue.take();
                       if(Token.EMPTY_TOKEN.equals(token)) {
                           tokensQueue.clear();
                           break;
                       }
                       LOGGER.info("Serving: {" + token +
                               ", ServiceCounter:" + this.counterName + ", KafkaTopic: "+ this.queueName+ "}");
                   } catch (InterruptedException e) {
                       LOGGER.error("Failed to get token to serve, Service counter "
                               + this.counterName + ", KafkaTopic: "+ this.queueName+ "}");
                   }
               }
            });

            subscribeToTopic();
            return;
        }
    }

    private void subscribeToTopic() {
        kafkaConsumer.subscribe(Collections.singletonList(queueName), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            }
        });

        isSubscribedToTopic.set(true);
        executorService.submit(() -> {
            while (isSubscribedToTopic.get()) {
                waitIfKafkaServiceIsNotRunning();
                try {
                    ConsumerRecords<String, ApplicantTokenRecord> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(100));
                    if(!consumerRecords.isEmpty()) {
                        for (ConsumerRecord<String, ApplicantTokenRecord> record : consumerRecords) {
                            ApplicantTokenRecord tokenRecord = record.value();
                            serveToken(tokenRecord.getToken());
                        }
                        kafkaConsumer.commitSync();
                    }

                } catch (Exception e) {
                    LOGGER.error(Thread.currentThread().getName()  +" failed to consume token records from kafka topic "
                            + this.queueName);
                }
            }
        });
    }

    private void waitIfKafkaServiceIsNotRunning() {
        if(!isKafkaServiceRunning.get()) {
            waitingThread.submit(() -> {
               while (!isKafkaServiceRunning.get()) {
                   try {
                       Thread.sleep(5000 );
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }
            });
        }
    }

    public void stopCounter() {
        if(isStarted.getAndSet(false)) {
            try {
                tokensQueue.put(Token.EMPTY_TOKEN);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executorService.shutdown();
            kafkaConsumer.unsubscribe();
            isSubscribedToTopic.set(false);
        }
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public String getName() {
        return counterName;
    }

    @Override
    public TokenCategory servingTokenCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceCounter that = (ServiceCounter) o;

        if (!Objects.equals(queueName, that.queueName)) return false;
        return Objects.equals(counterName, that.counterName);
    }

    @Override
    public int hashCode() {
        int result = queueName != null ? queueName.hashCode() : 0;
        result = 31 * result + (counterName != null ? counterName.hashCode() : 0);
        return result;
    }
}
