package zw.co.econet.smsgateway.persistence.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by oswin on 8/12/2016.
 */
@Data
@Entity
@Table(name = "sms_application", indexes = {
        @Index(name = "username_indx" , columnList = "username", unique = true)
})
public class SmsApplication {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 150)
    private String id;
    @Version
    private long version;
    @Column(name = "username",length = 20 , unique = true)
    private String username;
    @Column(name = "password",length = 20)
    private String password;
    @Column(name = "state")
    private String state;
    @Column(name = "notification_url")
    private String notificationUrl;
    @Column(name ="destination_code" , length = 20 , unique = true)
    private String destinationCode;
    @OneToMany
    @JoinTable(name = "application_role" )
    private List<SystemRole> systemRole;

}
