package zw.co.econet.smsgateway.persistence.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by oswin on 16/12/2016.
 */
@Data
@Entity
@Table(name = "system_role")
public class SystemRole  implements Serializable{

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 150)
    private String id;
    @Version
    private long version;
    @Column(name ="role_name", length = 30)
    private String roleName;
}
