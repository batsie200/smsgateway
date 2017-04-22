package zw.co.econet.smsgateway.persistence.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import zw.co.econet.smsgateway.persistence.SmppBindClass;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Data
@Table(name = "smpp_configuration")
public class SmppConfiguration implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 150)
    private String id;
    @Version
    private long version;
    @Column(name = "application", length = 50)
    private String applicationName;
    @Column(name = "hostname", length = 50)
    private String hostname;
    @Column(name = "port", length = 50)
    private int port;
    @Column(name = "system_id", length = 50)
    private String username;
    @Column(name = "system_password", length = 50)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "bind_type", length = 50)
    private SmppBindClass smppBindType;
    @Column(name = "account_state")
    private boolean state;

}
