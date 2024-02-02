package com.jdkendall.audit.processing;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/InputQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class MessageProcessorBean implements MessageListener {

    @Override
    public void onMessage(Message message) {
        System.out.println("Received message: " + message);
        // TODO: Implement message processing, transformation, and sending to output queue
    }
}
