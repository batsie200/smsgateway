package zw.co.econet.smsgateway.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.http.HttpStatus;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Created by oswin on 9/12/2016.
 */
@Data
@Entity
@XmlRootElement(name = "serviceError" )
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "rest_error", indexes = {
        @Index(name="error_class_idx" , columnList = "exception_class", unique = true),
        @Index(name="code_idx", columnList = "code", unique = true)
})
public class RestError {
    @Id
    @JsonIgnore
    @XmlTransient
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 150)
    private String id;
    @Version
    @XmlTransient
    @JsonIgnore
    private long version;
    @Column(name = "code", length = 20)
    @JsonProperty(value = "code")
    private String errorCode;
    @Column(name = "message")
    @JsonProperty(value = "message")
    private String errorMessage;
    @Column(name ="exception_class")
    @JsonIgnore
    @XmlTransient
    private String exceptionClass;
    @Column(name = "http_status", length = 50)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    @XmlTransient
    private HttpStatus httpStatus;
}
