package zw.co.econet.smsgateway.smpp.cloudhopper;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.LoadBalancedList;
import com.cloudhopper.commons.util.LoadBalancedLists;
import com.cloudhopper.commons.util.RoundRobinLoadBalancedList;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import zw.co.econet.smsgateway.persistence.model.SmppConfiguration;
import zw.co.econet.smsgateway.persistence.services.SmppConfigurationService;
import zw.co.econet.smsgateway.smpp.SmppService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static zw.co.econet.smsgateway.persistence.SmppBindClass.RECEIVER;
import static zw.co.econet.smsgateway.persistence.SmppBindClass.TRANSCEIVER;
import static zw.co.econet.smsgateway.persistence.SmppBindClass.TRANSMITTER;

@Slf4j
@Service
@Profile("cloudhopper")
public class CloudHopperSmppService implements SmppService {

    private LoadBalancedList<OutboundClient> balancedList;
    private final ApplicationContext context;

    private final SmppConfigurationService configurationFacade;

    @Autowired
    public CloudHopperSmppService(ApplicationContext context, SmppConfigurationService configurationFacade) {
        this.context = context;
        this.configurationFacade = configurationFacade;
    }

    @PostConstruct
    public void createSmppSession() {
        balancedList = LoadBalancedLists.synchronizedList(new RoundRobinLoadBalancedList<>());
        configurationFacade.findAll().stream().filter(SmppConfiguration::isState).forEach(smppConfiguration -> balancedList.set(createClient(context.getBean("smppClientMessageServiceBean", SmppClientMessageService.class), getSmppSessionConfiguration(smppConfiguration)), 1));
    }

    private SmppSessionConfiguration getSmppSessionConfiguration(SmppConfiguration smppConfiguration) {
        SmppSessionConfiguration config = new SmppSessionConfiguration();
        config.setWindowSize(50);
        config.setName(smppConfiguration.getApplicationName());
        switch (smppConfiguration.getSmppBindType()) {
            case RECEIVER:
                config.setType(SmppBindType.RECEIVER);
                break;
            case TRANSMITTER:
                config.setType(SmppBindType.TRANSMITTER);
                break;
            case TRANSCEIVER:
                config.setType(SmppBindType.TRANSCEIVER);
                break;
        }
        config.setPort(smppConfiguration.getPort());
        config.setHost(smppConfiguration.getHostname());
        config.setConnectTimeout(10000);
        config.setSystemId(smppConfiguration.getUsername());
        config.setPassword(smppConfiguration.getPassword());
        config.getLoggingOptions().setLogBytes(true);
        config.setRequestExpiryTimeout(30000);
        config.setWindowMonitorInterval(15000);
        config.setCountersEnabled(true);
        return config;
    }

    public Optional<String> sendSms(String destinationNumber, String sourceNumber, String message) {
        return (message.length() < 160) ? sendShortSms(destinationNumber, sourceNumber, message) : Optional.of(sendLongMessage(destinationNumber, sourceNumber, message).get().get(0));

    }

    public Optional<String> sendShortSms(String destinationNumber, String sourceNumber, String message) {
        String targetNumber = destinationNumber;
        if (destinationNumber.length() > 9)
             targetNumber = "263" + destinationNumber.substring(destinationNumber.length() - 9);
        try {
            final SmppSession session = balancedList.getNext().getSession();
            if (session != null && session.isBound()) {
                SubmitSm submit = new SubmitSm();
                submit.setSourceAddress(new Address((byte) 0x01, (byte) 0x01, sourceNumber));
                submit.setDestAddress(new Address((byte) 0x01, (byte) 0x01, targetNumber));
                submit.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);
                submit.setShortMessage(CharsetUtil.encode(message, CharsetUtil.CHARSET_GSM));
                final SubmitSmResp submitSmResp = session.submit(submit, 5000);
                log.info("Sms sent to {}. SubmitId {}", targetNumber, submitSmResp.getMessageId());
                return Optional.of(submitSmResp.getMessageId());
            } else {
                log.error("Failed to send sms. Check if the session is bound");
                return Optional.empty();
            }
        } catch (InterruptedException | SmppChannelException | RecoverablePduException | UnrecoverablePduException | SmppTimeoutException e) {
            log.error("Error encountered sending the sms : {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<List<String>> sendLongMessage(String destinationNumber, String sourceNumber, String message) {
        String targetNumber = destinationNumber;
        if (destinationNumber.length() > 9)
            targetNumber = "263" + destinationNumber.substring(destinationNumber.length() - 9);
        try {
            List<String> submitIds = new ArrayList<>();
            byte sourceTon = (byte) 0x01;
            if (sourceNumber != null && sourceNumber.length() > 0) {
                sourceTon = (byte) 0x05;
            }

            byte[] textBytes = CharsetUtil.encode(message, CharsetUtil.CHARSET_ISO_8859_15);

            int maximumMultipartMessageSegmentSize = 134;
            byte[][] byteMessagesArray = splitUnicodeMessage(textBytes, maximumMultipartMessageSegmentSize);
            // submit all messages
            final SmppSession session = balancedList.getNext().getSession();
            if (session != null && session.isBound()) {
                for (byte[] aByteMessagesArray : byteMessagesArray) {
                    SubmitSm submit0 = new SubmitSm();
                    submit0.setEsmClass(SmppConstants.ESM_CLASS_UDHI_MASK);
                    submit0.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);
                    submit0.setSourceAddress(new Address(sourceTon, (byte) 0x01, sourceNumber));
                    submit0.setDestAddress(new Address((byte) 0x01, (byte) 0x01, targetNumber));
                    submit0.setShortMessage(aByteMessagesArray);
                    final SubmitSmResp submitSmResp = session.submit(submit0, 10000);
                    log.info("sms sent to {}. SubmitId {}", destinationNumber, submitSmResp.getMessageId());
                    submitIds.add(submitSmResp.getMessageId());
                }
            }
            return Optional.of(submitIds);
        } catch (InterruptedException | SmppChannelException | RecoverablePduException | UnrecoverablePduException | SmppTimeoutException e) {
            log.error("Error encountered sending the sms : {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static byte[][] splitUnicodeMessage(byte[] aMessage, Integer maximumMultipartMessageSegmentSize) {
        final byte UDHIE_HEADER_LENGTH = 0x05;
        final byte UDHIE_IDENTIFIER_SAR = 0x00;
        final byte UDHIE_SAR_LENGTH = 0x03;

        // determine how many messages have to be sent
        int numberOfSegments = aMessage.length / maximumMultipartMessageSegmentSize;
        int messageLength = aMessage.length;
        if (numberOfSegments > 255) {
            numberOfSegments = 255;
            messageLength = numberOfSegments * maximumMultipartMessageSegmentSize;
        }
        if ((messageLength % maximumMultipartMessageSegmentSize) > 0) {
            numberOfSegments++;
        }

        // prepare array for all of the msg segments
        byte[][] segments = new byte[numberOfSegments][];

        int lengthOfData;

        // generate new reference number
        byte[] referenceNumber = new byte[1];
        new Random().nextBytes(referenceNumber);

        // split the message adding required headers
        for (int i = 0; i < numberOfSegments; i++) {
            if (numberOfSegments - i == 1) {
                lengthOfData = messageLength - i * maximumMultipartMessageSegmentSize;
            } else {
                lengthOfData = maximumMultipartMessageSegmentSize;
            }
            // new array to store the header
            segments[i] = new byte[6 + lengthOfData];

            // UDH header
            // doesn't include itself, its header length
            segments[i][0] = UDHIE_HEADER_LENGTH;
            // SAR identifier
            segments[i][1] = UDHIE_IDENTIFIER_SAR;
            // SAR length
            segments[i][2] = UDHIE_SAR_LENGTH;
            // reference number (same for all messages)
            segments[i][3] = referenceNumber[0];
            // total number of segments
            segments[i][4] = (byte) numberOfSegments;
            // segment number
            segments[i][5] = (byte) (i + 1);
            // copy the data into the array
            System.arraycopy(aMessage, (i * maximumMultipartMessageSegmentSize), segments[i], 6, lengthOfData);
        }
        return segments;
    }


    private OutboundClient createClient(SmppClientMessageService smppClientMessageService, SmppSessionConfiguration smppSessionConfiguration) {
        OutboundClient client = (OutboundClient) context.getBean("outboundClient");
        client.initialize(smppSessionConfiguration, smppClientMessageService);
        client.scheduleReconnect();
        return client;
    }
}
