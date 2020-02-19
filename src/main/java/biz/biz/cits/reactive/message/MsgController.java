package biz.biz.cits.reactive.message;


import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("mq")
public class MsgController {

    @Autowired
    private JmsTemplate jmsTemplate;


    @GetMapping(path = "send", produces = "application/json")
    public String sendMessages(@RequestParam int numMessage) {
        ArrayList<Map.Entry<String, String>> messages = MsgGenerator.getMessages(numMessage);
        messages.forEach((e) -> jmsTemplate.send(new ActiveMQQueue("in-queue"), messageCreator -> {
            TextMessage message = messageCreator.createTextMessage(e.getValue());
            message.setJMSCorrelationID(e.getKey());
            return message;
        }));
        return"done";
}

}