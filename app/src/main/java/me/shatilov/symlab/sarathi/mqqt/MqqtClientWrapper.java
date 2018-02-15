package me.shatilov.symlab.sarathi.mqqt;

import android.content.Context;
import android.support.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import me.shatilov.utility.EasyCallable;

/**
 * Created by Kirill on 24-Jan-18.
 */

public class MqqtClientWrapper {

    private MqttAndroidClient mqttAndroidClient;
    private Context context;

    private String serverUri;
    private String subscriptionTopic;
    private String clientID;

    private EasyCallable<String> notificationChannel;
    private EasyCallable<String> messageHandler;

    public MqqtClientWrapper(Context context,
                             @Nullable String serverUri,
                             @Nullable String subscriptionTopic,
                             @Nullable String clientID,
                             EasyCallable<String> notificationChannel,
                             EasyCallable<String> messageHandler) {
        this.context = context;
        this.serverUri = serverUri;
        this.subscriptionTopic = subscriptionTopic;
        this.clientID = clientID;
        this.notificationChannel = notificationChannel;
        this.messageHandler = messageHandler;
    }


    public MqqtClientWrapper setServerUri(String serverUri) {
        this.serverUri = serverUri;
        return this;
    }

    public MqqtClientWrapper setSubscriptionTopic(String subscriptionTopic) {
        this.subscriptionTopic = subscriptionTopic;
        return this;
    }

    public MqqtClientWrapper setClientID(String clientID) {
        this.clientID = clientID;
        return this;
    }

    public MqqtClientWrapper setNotificationChannel(EasyCallable<String> notificationChannel) {
        this.notificationChannel = notificationChannel;
        return this;
    }

    public MqqtClientWrapper setMessageHandler(EasyCallable<String> messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    public MqqtClientWrapper connect() {

        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientID);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    notificationChannel.accept("Reconnected to: " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    notificationChannel.accept("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                notificationChannel.accept("The connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                messageHandler.accept(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    notificationChannel.accept("Failed to connect to: " + serverUri);
                }
            });

        } catch (MqttException ex) {
            notificationChannel.accept(ex.getMessage());
        }
        return this;
    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    notificationChannel.accept("Subscribed to: " + serverUri + subscriptionTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    notificationChannel.accept("Failed to subscribe");
                }
            });

        } catch (MqttException ex) {
            notificationChannel.accept(ex.getMessage());
        }
    }

    public MqqtClientWrapper publishMessage(String publishMessage) {
        return this.publishMessage(publishMessage, false);
    }

    public MqqtClientWrapper publishMessage(String publishMessage, boolean retained) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            message.setRetained(retained);
            mqttAndroidClient.publish(subscriptionTopic, message);
            notificationChannel.accept("Message Published");
            if (!mqttAndroidClient.isConnected() && mqttAndroidClient != null) {
                notificationChannel.accept(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            notificationChannel.accept("Error Publishing: " + e.getMessage());
        }
        return this;
    }

    public void disconnect() {
        try {
            mqttAndroidClient.disconnect();
            mqttAndroidClient.unregisterResources();
        } catch (MqttException e) {
            notificationChannel.accept("Disconnect failed:" + e.getMessage());
        }
    }
}
