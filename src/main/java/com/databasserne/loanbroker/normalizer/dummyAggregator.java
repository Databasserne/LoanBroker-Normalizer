/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.databasserne.loanbroker.normalizer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kasper S. Worm
 */
public class dummyAggregator {

    public static String QUEUE_NAME;
    private final static String SEND_NAME = "Databasserne_Aggregator";
//    private final static String HOST_NAME = "10.18.144.10";
//    private final static String HOST_NAME = "datdb.cphbusiness.dk";
    private final static String HOST_NAME = "5.179.80.218";
    
    public static void main(String[] args) throws IOException, ShutdownSignalException, InterruptedException, ConsumerCancelledException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();

        Channel receiveChannel = connection.createChannel();
        receiveChannel.queueDeclare("Databasserne_Aggregator", true, false, false, null);
        String replyQueue = "Databasserne_Aggregator";

        receive(receiveChannel, replyQueue);
    }

    private static void receive(Channel chan, String queue) throws ShutdownSignalException, InterruptedException, ConsumerCancelledException, TimeoutException, IOException {
        System.out.println("***RECEIVING MESSAGES FROM " + queue + "***");

        // Receive the reply message
        Consumer qc = new DefaultConsumer(chan) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String receivedMessage = new String(body);
                System.out.println("\n***MESSAGE RECEIVED***");
                System.out.println("ID: " + properties.getCorrelationId());
                System.out.println("Received: " + receivedMessage);
                chan.basicAck(envelope.getDeliveryTag(), false);

//                try {
//                    //TODO - format to single output form and send to aggregator
////                    send(messageToJson(properties.getCorrelationId(), receivedMessage));
//                    System.out.println("modtaget");
//                } catch (TimeoutException ex) {
//                    Logger.getLogger(Normalizer.class.getName()).log(Level.SEVERE, null, ex);
//                }

            }
        };
        chan.basicConsume(queue, false, qc);

    }

}
