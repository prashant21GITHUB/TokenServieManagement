package com.brillio.tms.token_service_mgmt.tokenService;

import com.brillio.tms.token_service_mgmt.IAppService;
import com.brillio.tms.token_service_mgmt.TMSConfig;
import com.brillio.tms.token_service_mgmt.annotation.AppService;
import com.brillio.tms.token_service_mgmt.enums.TokenCategory;
import com.brillio.tms.token_service_mgmt.kafka.KafkaConsumerService;
import com.brillio.tms.token_service_mgmt.kafka.KafkaMonitorService;
import com.brillio.tms.token_service_mgmt.kafka.KafkaTopicService;
import com.brillio.tms.token_service_mgmt.models.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 *  It reads the service counter ids and queue names from following application properties:
 *  {@literal service.counter.queue.names}
 * and  {@literal service.counter.id.category.pairs}
 *
 * It then creates the service counter instances and also start and stop counters.
 * See {@link ServiceCounter#startCounter()} and {@link ServiceCounter#stopCounter()}
 *
 *  It also balances the load on each service counter by assigning the token request to a service counter
 *  by calculating mod using below formula.
 *
 *      tokenNumber % (No. of service counters in corresponding token category)
 *
 * @see {@link TokenCategory}
 *
 */
@AppService
@Repository
public class ServiceCounterRegistry implements IServiceCounterRegistryService, IAppService {

    private final String[] serviceCounterList;
    private final String[] serviceCounterKafkaTopics;

    private Map<Integer, ServiceCounter> normalCategoryCounters;
    private Map<Integer, ServiceCounter> premiumCategoryCounters;
    private final KafkaTopicService kafkaTopicService;
    private final KafkaConsumerService kafkaConsumerService;
    private final KafkaMonitorService kafkaMonitorService;

    @Autowired
    public ServiceCounterRegistry(KafkaTopicService topicConfig,
                                  KafkaConsumerService kafkaConsumerService,
                                  KafkaMonitorService kafkaMonitorService,
                                  TMSConfig config) {
        this.serviceCounterList = config.getServiceCounterList();
        this.serviceCounterKafkaTopics = config.getServiceCounterQueueNames();
        this.kafkaTopicService = topicConfig;
        this.kafkaConsumerService = kafkaConsumerService;
        this.kafkaMonitorService = kafkaMonitorService;
        if(!checkValidityForCounterList()) {
            throw new IllegalArgumentException("Please provide service counter list and corresponding kafka topics");
        }
        createServiceCounters();
    }

    private void createServiceCounters() {
        this.premiumCategoryCounters = new HashMap<>();
        this.normalCategoryCounters = new HashMap<>();
        String arr[];
        int index = 0, normalCounterCount = 0, premiumCounterCount = 0;
        for(String counterCategoryPair : serviceCounterList) {
            arr = counterCategoryPair.split(":");
            TokenCategory category = TokenCategory.parse(arr[1]);
            ServiceCounter serviceCounter = new ServiceCounter(category, arr[0],
                    serviceCounterKafkaTopics[index], kafkaConsumerService, kafkaMonitorService);
            if(category.equals(TokenCategory.PREMIUM)) {
                premiumCategoryCounters.put(premiumCounterCount++, serviceCounter);
            } else {
                normalCategoryCounters.put(normalCounterCount++, serviceCounter);
            }
            index++;
        }
    }

    private boolean checkValidityForCounterList() {
        if(serviceCounterList == null || serviceCounterList.length == 0 ||
                serviceCounterKafkaTopics == null ||
                serviceCounterKafkaTopics.length != serviceCounterList.length) {
            return false;
        }
        return true;
    }

    public IServiceCounter getServiceCounterForToken(Token token) {
        int tokenNumber = token.getTokenNumber();
        TokenCategory tokenCategory = token.getTokenCategory();
        int key;
        if(TokenCategory.PREMIUM.equals(tokenCategory)) {
            key = tokenNumber % premiumCategoryCounters.size();
            return premiumCategoryCounters.get(key);
        } else {
            key = tokenNumber % normalCategoryCounters.size();
            return normalCategoryCounters.get(key);
        }
    }

    @Override
    public void start() {
        startCounters(premiumCategoryCounters);
        startCounters(normalCategoryCounters);
    }

    @Override
    public void stop() {
        stopCounters(normalCategoryCounters);
        stopCounters(premiumCategoryCounters);
    }

    private void startCounters(Map<Integer, ServiceCounter> countersMap) {
        for(ServiceCounter counter : countersMap.values()) {
            counter.startCounter();
            kafkaTopicService.createTopic(counter.getQueueName());
        }
    }

    private void stopCounters(Map<Integer, ServiceCounter> countersMap) {
        for(ServiceCounter counter : countersMap.values()) {
            counter.stopCounter();
        }
    }

}
