package zw.co.econet.smsgateway.persistence.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import zw.co.econet.smsgateway.util.MessageState;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by developer on 9/6/14.
 */
@XmlRootElement
@Data
@Entity
@Table(name = "sms", indexes = {
        @Index(name = "source_add_idx" , columnList = "source"),
        @Index(name="correlator_id_idx", unique = true, columnList = "client_correlator"),
        @Index(name ="submit_idx", unique = true, columnList = "submit_id"),
        @Index(name ="server_ref_idx" , unique = true, columnList = "server_reference")
})
public class Sms implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @XmlTransient
    @JsonIgnore
    @Column(name = "id", length = 150)
    private String id;
    @Version
    @JsonIgnore
    private long version;
    @Column(name ="source" , length = 30)
    private String sourceAddress;
    @Column(name ="destination" , length = 30)
    private String destinationNumber;
    @Column(name = "notification_url")
    private String notificationUrl;
    @Column(name ="submit_id" , length = 50)
    private String submitId;
    @Column(name ="delivery_state" , length = 15)
    @Enumerated(EnumType.STRING)
    private MessageState deliveryState;
    @Column(name ="collection_state", length = 15)
    @Enumerated(EnumType.STRING)
    private MessageState collectionState;
    @Column(name ="time_received")
    private LocalDateTime timeReceived;
    @Column(name ="time_routed")
    private LocalDateTime timeRouted;
    @Column(name ="time_delivered")
    private LocalDateTime deliveryTime;
    @Transient
    private String message;
    @Column(name ="notify_url" , length = 120)
    private String notifyUrl;
    @Column(name ="sender" , length = 50)
    private String sender;
    @Column(name ="client_correlator", length = 100)
    private String clientCorrelator;
    @Column(name ="server_reference" , length = 100)
    private String serverReference;

}
