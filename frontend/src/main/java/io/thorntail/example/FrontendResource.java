package io.thorntail.example;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
public class FrontendResource {
    private static final Logger log = Logger.getLogger(FrontendResource.class);

    @Inject
    private GlobalData globalData;

    @Inject
    private JMSContext jmsContext;

    @POST
    @Path("/send-request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String sendRequest(Request request) {
        log.infof("%s: Sending %s", globalData.id, request);

        Queue requests = jmsContext.createQueue("work-queue/requests");
        Queue responses = jmsContext.createQueue("work-queue/responses");

        try {
            TextMessage message = jmsContext.createTextMessage();
            message.setJMSReplyTo(responses);
            message.setBooleanProperty("uppercase", request.isUppercase());
            message.setBooleanProperty("reverse", request.isReverse());
            message.setText(request.getText());

            jmsContext.createProducer().send(requests, message);

            globalData.data.requestIds.add(message.getJMSMessageID());

            return message.getJMSMessageID();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/receive-response")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveResponse(@QueryParam("request") String requestId) {
        if (requestId == null) {
            throw new BadRequestException("A 'request' parameter is required");
        }

        Response response = globalData.data.responses.get(requestId);

        if (response == null) {
            throw new NotFoundException();
        }

        return response;
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Data getGlobalData() {
        return globalData.data;
    }
}
