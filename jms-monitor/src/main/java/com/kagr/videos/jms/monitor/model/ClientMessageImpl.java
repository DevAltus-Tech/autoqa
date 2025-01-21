package com.kagr.videos.jms.monitor.model;





import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;





@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientMessageImpl {
    private int messageID;
    private boolean durable;
    private String address;
    private String userID;
    private TypedProperties properties;
}
