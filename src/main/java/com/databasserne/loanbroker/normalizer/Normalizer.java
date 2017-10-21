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
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author Kasper S. Worm
 */
public class Normalizer {

    private final static String SEND_NAME = "Databasserne_Aggregator";
    private final static String HOST_NAME = "datdb.cphbusiness.dk";
//    private final static String HOST_NAME = "10.18.144.10";
//    private final static String HOST_NAME = "5.179.80.218";

    /**
     * Listens to response from the bank
     *
     * @param args
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     * @throws ShutdownSignalException
     * @throws InterruptedException
     * @throws ConsumerCancelledException
     */
    public static void main(String[] args) throws IOException, TimeoutException, ShutdownSignalException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();

        Channel receiveChannel = connection.createChannel();
        receiveChannel.queueDeclare("Databasserne_Normalizer", true, false, false, null);
        String replyQueue = "Databasserne_Normalizer";

        receive(receiveChannel, replyQueue);

    }

    /**
     * Listen to messages from Databasserne_Normalizer queue
     *
     * @param chan channel to consume from
     * @param queue queue to consume from
     * @throws ShutdownSignalException
     * @throws InterruptedException
     * @throws ConsumerCancelledException
     * @throws TimeoutException
     * @throws IOException
     */
    private static void receive(Channel chan, String queue) throws ShutdownSignalException, InterruptedException, ConsumerCancelledException, TimeoutException, IOException {
        System.out.println("***RECEIVING MESSAGES FROM " + queue + "***");

        // Receive the reply message
        Consumer qc = new DefaultConsumer(chan) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String receivedMessage = new String(body);
                System.out.println("\n******");
                System.out.println("ID: " + properties.getCorrelationId());
                System.out.println("Received: " + receivedMessage);
                chan.basicAck(envelope.getDeliveryTag(), false);

                try {
                    //TODO - format to single output form and send to aggregator
                    send(messageToJson(properties.getCorrelationId(), receivedMessage));
                } catch (TimeoutException ex) {
                    Logger.getLogger(Normalizer.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };
        chan.basicConsume(queue, false, qc);

    }

    /**
     * Converts message to correct JSON format
     *
     * @param id bank id
     * @param message the message to be converted
     * @return converted JSON
     */
    private static String messageToJson(String id, String message) {
        String ssn;
        double rate;

        //Check if XML or JSON format, and extract values
        if (id.contains("XML")) {
            JSONObject xml = XML.toJSONObject(message);
            JSONObject lr = xml.getJSONObject("LoanResponse");
            ssn = lr.get("ssn").toString();
            rate = lr.getDouble("interestRate");
        } else {
            JSONObject json = new JSONObject(message);
            ssn = json.get("ssn").toString();
            rate = json.getDouble("interestRate");
        }

        //Check if ssn contains "-" if not, add it.
        if (!ssn.contains("-")) {
            String tempSSN = ssn;
            String ssn1 = tempSSN.substring(0, 6);
            String ssn2 = tempSSN.substring(6, 10);
            String ssnFull = ssn1 + "-" + ssn2;
            ssn = ssnFull;
        }

        //Set message together and return it
        String temp
                = "{\"ssn\":\"" + ssn + "\","
                + "\"interestRate\":" + rate + ","
                + "\"bank\":\"" + id + "\"}";
        return temp;
    }

    /**
     * Sends message to Aggregator
     *
     * @param message message to send
     * @throws IOException
     * @throws TimeoutException
     */
    private static void send(String message) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel aggregatorChannel = connection.createChannel();

        aggregatorChannel.queueDeclare(SEND_NAME, true, false, false, null);

        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties()
                .builder()
                .correlationId("Normalizer")
                .build();

        System.out.println("Sent to Aggregator: " + message);
        System.out.println("******");
        aggregatorChannel.basicPublish("", SEND_NAME, basicProperties, message.getBytes());

        aggregatorChannel.close();
        connection.close();
    }
}
