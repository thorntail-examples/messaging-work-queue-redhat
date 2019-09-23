/*
 *
 *  Copyright 2018-2019 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package io.thorntail.example;

import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "factory1"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "work-queue"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "work-queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic1"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://${env.MESSAGING_SERVICE_HOST:localhost}:${env.MESSAGING_SERVICE_PORT:5672};topic.topic1=work-queue/worker-updates"),
})
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WorkerUpdateListener implements MessageListener {
    private static final Logger log = Logger.getLogger(WorkerUpdateListener.class);

    @Inject
    private GlobalData globalData;

    @Override
    public void onMessage(Message message) {
        WorkerUpdate update;

        try {
            String workerId = message.getStringProperty("workerId");
            long timestamp = message.getLongProperty("timestamp");
            long requestsProcessed = message.getLongProperty("requestsProcessed");
            long processingErrors = message.getLongProperty("processingErrors");

            update = new WorkerUpdate(workerId, timestamp, requestsProcessed, processingErrors);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        globalData.data.workers.put(update.getWorkerId(), update);

        log.debugf("%s: Received %s", globalData.id, update);
    }
}
