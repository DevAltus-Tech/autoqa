package com.kagr.videos.jms.monitor.model;





import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;





@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypedProperties {
    private int bindingID;
    private long notifTimestamp;
    private int bindingType;
    private String routingName;
    private int distance;
    private String clusterName;
    private String address;
    private String notifType;
    private int consumerCount;
    private String user;
    private String sessionName;
    private String remoteAddress;
    private int consumerName;
    private String clientID;
    private String certSubjectDN;
    private String validatedUser;
}
