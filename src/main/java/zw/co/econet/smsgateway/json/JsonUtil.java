package zw.co.econet.smsgateway.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: oswin
 * Date: 9/6/13
 * Time: 9:33 AM
 * To change this template use File | Settings | File Templates.
 */
@Service
@Slf4j
public class JsonUtil {

    public String convertToJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse {}", e.getMessage());
            return null;
        }
    }

    public <T> T convertToObject(String jsonString, T object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return (T) mapper.readValue(jsonString, object.getClass());
        } catch (IOException e) {
            log.error("Failed to convert json string : {}  to object of type : {} ", jsonString, object.getClass());
            return null;
        }
    }

    public String convertToXml(Object source, Class... type) {
        String result;
        StringWriter stringWriter = new StringWriter();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(type);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(source, stringWriter);
            result = stringWriter.toString();
        } catch (JAXBException e) {
            log.error("Failed to marshal to XML error {}", e);
            return null;
        }
        return result;
    }
}
