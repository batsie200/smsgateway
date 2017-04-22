package zw.co.econet.smsgateway.amqp;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 * User: ozie
 * Date: 8/15/12
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
@Service("amqpSink")
@Slf4j
public class AmqpSink {

    private final RabbitTemplate template;

    @Autowired
    public AmqpSink(RabbitTemplate template) {
        this.template = template;
    }

    @Async
    public void sendTextMessage(String exchange, String routingKey, String textMessage) {
        log.info("Sending message to : Exchange = {} | Routing key = {} | Message = {}", exchange, routingKey, textMessage);
        template.convertAndSend(exchange, routingKey, textMessage);
    }

    @Async
    public void sendMessage(String exchange, String routingKey, Object message) {
        log.info("Sending message to : Exchange = {} | Routing key = {} | Message = {}", exchange, routingKey, message);
        template.convertAndSend(exchange, routingKey, message);
    }
}
