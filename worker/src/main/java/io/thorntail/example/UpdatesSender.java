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

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;

@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UpdatesSender {
    private static final Logger log = Logger.getLogger(UpdatesSender.class);

    @Inject
    private GlobalData globalData;

    @Inject
    private JMSContext jmsContext;

    @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    public void sendUpdate() {
        log.debugf("%s: Sending status update", globalData.id);

        Message message = jmsContext.createMessage();
        try {
            message.setStringProperty("workerId", globalData.id);
            message.setLongProperty("timestamp", System.currentTimeMillis());
            message.setLongProperty("requestsProcessed", globalData.requestsProcessed.get());
            message.setLongProperty("processingErrors", globalData.processingErrors.get());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        Topic workerStatus = jmsContext.createTopic("work-queue/worker-updates");
        jmsContext.createProducer().send(workerStatus, message);
    }
}
