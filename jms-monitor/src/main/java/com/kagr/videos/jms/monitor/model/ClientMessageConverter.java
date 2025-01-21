package com.kagr.videos.jms.monitor.model;





import java.util.regex.Matcher;
import java.util.regex.Pattern;





public class ClientMessageConverter {

    public static ClientMessageImpl parseStringToClientMessageImpl(String input) {
        Pattern mainPattern = Pattern.compile("ClientMessageImpl\\[(.*?)\\]");
        Matcher mainMatcher = mainPattern.matcher(input);

        if (mainMatcher.find()) {
            String[] mainParts = mainMatcher.group(1).split(", ");
            ClientMessageImpl message = new ClientMessageImpl();
            TypedProperties properties = new TypedProperties();

            for (String part : mainParts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    switch (key) {
                        case "messageID":
                            message.setMessageID(Integer.parseInt(value));
                            break;
                        case "durable":
                            message.setDurable(Boolean.parseBoolean(value));
                            break;
                        case "address":
                            message.setAddress(value);
                            break;
                        case "userID":
                            message.setUserID(value);
                            break;
                        case "properties":
                            properties = parseProperties(value);
                            break;
                    }
                }
            }
            message.setProperties(properties);
            return message;
        }

        return null;
    }





    private static TypedProperties parseProperties(String propertiesString) {
        TypedProperties properties = new TypedProperties();
        Pattern propertiesPattern = Pattern.compile("TypedProperties\\[(.*?)\\]");
        Matcher propertiesMatcher = propertiesPattern.matcher(propertiesString);

        if (propertiesMatcher.find()) {
            String[] props = propertiesMatcher.group(1).split(", ");
            for (String prop : props) {
                String[] keyValue = prop.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    assignProperty(properties, key, value);
                }
            }
        }
        return properties;
    }





    private static void assignProperty(TypedProperties properties, String key, String value) {
        switch (key) {
            case "_AMQ_Binding_ID":
                properties.setBindingID(Integer.parseInt(value));
                break;
            case "_AMQ_NotifTimestamp":
                properties.setNotifTimestamp(Long.parseLong(value));
                break;
            case "_AMQ_Binding_Type":
                properties.setBindingType(Integer.parseInt(value));
                break;
            case "_AMQ_RoutingName":
                properties.setRoutingName(value);
                break;
            case "_AMQ_Distance":
                properties.setDistance(Integer.parseInt(value));
                break;
            case "_AMQ_ClusterName":
                properties.setClusterName(value);
                break;
            case "_AMQ_Address":
                properties.setAddress(value);
                break;
            case "_AMQ_NotifType":
                properties.setNotifType(value);
                break;
            case "_AMQ_ConsumerCount":
                properties.setConsumerCount(Integer.parseInt(value));
                break;
            case "_AMQ_User":
                properties.setUser(value);
                break;
            case "_AMQ_SessionName":
                properties.setSessionName(value);
                break;
            case "_AMQ_RemoteAddress":
                properties.setRemoteAddress(value);
                break;
            case "_AMQ_ConsumerName":
                properties.setConsumerName(Integer.parseInt(value));
                break;
            case "_AMQ_Client_ID":
                properties.setClientID(value);
                break;
            case "_AMQ_CertSubjectDN":
                properties.setCertSubjectDN(value);
                break;
            case "_AMQ_ValidatedUser":
                properties.setValidatedUser(value);
                break;
        }
    }
}
