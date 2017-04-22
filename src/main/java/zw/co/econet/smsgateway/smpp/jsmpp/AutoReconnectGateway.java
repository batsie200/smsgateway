/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package zw.co.econet.smsgateway.smpp.jsmpp;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@Service
@Scope("prototype")
@Profile("jsmpp")
public class AutoReconnectGateway {
    private static final Logger logger = LoggerFactory.getLogger(AutoReconnectGateway.class);
    private final TimeFormatter timeFormatter = new AbsoluteTimeFormatter();
    private SMPPSession session = null;
    private String remoteIpAddress;
    private int remotePort;
    private BindParameter bindParam;
    private final long reconnectInterval = 10000L; // 10 seconds

    /**
     * Construct auto reconnect gateway with specified ip address, port and SMPP
     * Bind parameters.
     *
     * @param remoteIpAddress is the SMSC IP address.
     * @param remotePort      is the SMSC port.
     * @param bindParam       is the SMPP Bind parameters.
     */
    public void initialize(String remoteIpAddress, int remotePort, BindParameter bindParam) {
        while (session== null || !session.getSessionState().isBound()) {
            this.remoteIpAddress = remoteIpAddress;
            this.remotePort = remotePort;
            this.bindParam = bindParam;
            try {
                session = newSession();
                if (session.getSessionState().isBound()) {
                    return;
                }
            } catch (IOException e) {
                logger.error("Failed to create an smpp session. Error {} ", e.getMessage());
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void startMessageListener(JsmppSmsReceiver messageListener) {
        try {
            getSession().setMessageReceiverListener(messageListener);
        } catch (IOException e) {
            logger.error("Failed to start message receiver . Error message {}", e.getMessage());
        }
    }

    /**
     * Create new {@link SMPPSession} complete with the
     * {@link SessionStateListenerImpl}.
     *
     * @return the {@link SMPPSession}.
     * @throws IOException if the creation of new session failed.
     */
    private SMPPSession newSession() throws IOException {
        SMPPSession tmpSession = new SMPPSession(remoteIpAddress, remotePort, bindParam);
        tmpSession.addSessionStateListener(new SessionStateListenerImpl());
        return tmpSession;
    }

    /**
     * Get the session. If the session still null or not in bound state, then IO
     * exception will be thrown.
     *
     * @return the valid session.
     * @throws IOException if there is no valid session or session creation is
     *                     invalid.
     */
    private SMPPSession getSession() throws IOException {
        if (session == null) {
            logger.info("Initiate session for the first time to " + remoteIpAddress + ":" + remotePort);
            session = newSession();
        } else if (!session.getSessionState().isBound()) {
            throw new IOException("We have no valid session yet");
        }
        return session;
    }

    /**
     * Reconnect session after specified interval.
     *
     * @param timeInMillis is the interval.
     */
    private void reconnectAfter(final long timeInMillis) {
        new Thread() {
            @Override
            public void run() {
                logger.info("Schedule reconnect after " + timeInMillis + " millis");
                try {
                    TimeUnit.MILLISECONDS.sleep(timeInMillis);
                } catch (InterruptedException ignored) {
                }

                int attempt = 0;
                while (session == null || session.getSessionState().equals(SessionState.CLOSED)) {
                    try {
                        logger.info("Reconnecting attempt #" + (++attempt) + "...");
                        session = newSession();
                    } catch (IOException e) {
                        logger.error("Failed opening connection and bind to " + remoteIpAddress + ":" + remotePort, e);
                        // wait for a second
                        try {
                            TimeUnit.MILLISECONDS.sleep(timeInMillis);
                        } catch (InterruptedException ee) {
                            logger.error("Failed to sleep for restart the client ");
                        }
                    }
                }
            }
        }.start();
    }

    public String sendSms(String destinationNumber, String sourceNumber, String message) {
        String[] msgId;
        int splitSize = 160;
        int totalSegments;
        String messageSubmitId = null;

        message = message.trim();
        if (!message.isEmpty()) {
            if (message.length() > 160) {
                totalSegments = getTotalSegmentsForTextMessage(message);
                Random random = new Random();
                OptionalParameter sarMsgRefNum = OptionalParameters.newSarMsgRefNum((short) random.nextInt());
                OptionalParameter sarTotalSegments = OptionalParameters.newSarTotalSegments(totalSegments);
                String[] segmentData = splitIntoStringArray(message, splitSize, totalSegments);
                msgId = new String[totalSegments];
                for (int segmentCounter = 0, sequenceNumber; segmentCounter < totalSegments; segmentCounter++) {
                    sequenceNumber = segmentCounter + 1;
                    OptionalParameter sarSegmentSeqnum = OptionalParameters.newSarSegmentSeqnum(sequenceNumber);
                    try {
                        msgId[segmentCounter] = getSession().submitShortMessage("CMT", TypeOfNumber.ALPHANUMERIC, NumberingPlanIndicator.ISDN, sourceNumber, TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, destinationNumber, new ESMClass(), (byte) 0, (byte) 1, timeFormatter.format(new Date()), null, new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE), (byte) 0, new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false), (byte) 0, segmentData[segmentCounter].getBytes(), sarMsgRefNum, sarSegmentSeqnum, sarTotalSegments);
                        logger.info("Message submitted| message_id {} | segment {} of {} | source : {} | destination | message : {} ", msgId[segmentCounter], sequenceNumber, totalSegments, sourceNumber, destinationNumber, segmentData[segmentCounter]);
                    } catch (PDUException e) {
                        logger.error("PDUException has occured {}", e.getMessage());
                    } catch (ResponseTimeoutException e) {
                        logger.error("ResponseTimeoutException has occured {}", e.getMessage());
                    } catch (InvalidResponseException e) {
                        logger.error("InvalidResponseException has occured {}", e.getMessage());
                    } catch (NegativeResponseException e) {
                        logger.error("NegativeResponseException has occured {}", e.getMessage());
                    } catch (IOException e) {
                        logger.error("IOException has occured {}", e.getMessage());
                    }
                }
            } else {
                try {
                    //
                    messageSubmitId = getSession().submitShortMessage("CMT", TypeOfNumber.ALPHANUMERIC, NumberingPlanIndicator.ISDN, sourceNumber, TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, destinationNumber, new ESMClass(), (byte) 0, (byte) 1, timeFormatter.format(new Date()), null, new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE), (byte) 0, new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false), (byte) 0, message.getBytes());
                    logger.info("Message submitted| message_id {} | source : {} | destination: {} | message : {} ", messageSubmitId, sourceNumber, destinationNumber, message);
                } catch (PDUException e) {
                    logger.error("PDUException has occured {}", e.getMessage());
                } catch (ResponseTimeoutException e) {
                    logger.error("ResponseTimeoutException has occured {}", e.getMessage());
                } catch (InvalidResponseException e) {
                    logger.error("InvalidResponseException has occured {}", e.getMessage());
                } catch (NegativeResponseException e) {
                    logger.error("NegativeResponseException has occured {}", e.getMessage());
                } catch (IOException e) {
                    logger.error("IOException has occured {}", e.getMessage());
                }
            }
        }
        return messageSubmitId;
    }

    private int getTotalSegmentsForTextMessage(String message) {
        int splitPostion = 160;
        int totalSegments = 1;
        if (message.length() > splitPostion) {
            totalSegments = (message.length() / splitPostion) + ((message.length() % splitPostion > 0) ? 1 : 0);
        }
        return totalSegments;
    }

    private String[] splitIntoStringArray(String textMessage, int position, int totalSegments) {
        String[] segmentData = new String[totalSegments];
        if (totalSegments > 1) {
            int splitPosition = position;
            int startIndex = 0;
            segmentData[startIndex] = "";
            segmentData[startIndex] = textMessage.substring(startIndex, splitPosition);
            for (int i = 1; i < totalSegments; i++) {
                segmentData[i] = "";
                startIndex = splitPosition;
                if (textMessage.length() - startIndex <= position) {
                    segmentData[i] = textMessage.substring(startIndex, textMessage.length());
                } else {
                    splitPosition = startIndex + position;
                    segmentData[i] = textMessage.substring(startIndex, splitPosition);
                }
            }
        }
        return segmentData;
    }

    /**
     * This class will receive the notification from {@link SMPPSession} for the
     * state changes. It will schedule to re-initialize session.
     *
     * @author uudashr
     */
    private class SessionStateListenerImpl implements SessionStateListener {
        @Override
        public void onStateChange(SessionState newState, SessionState oldState, Session source) {
            if (newState.equals(SessionState.CLOSED)) {
                logger.info("Session closed");
                reconnectAfter(reconnectInterval);
            }
        }
    }

    @Override
    public String toString() {
        return "AutoReconnectGateway{" +
                "session=" + session +
                ", remoteIpAddress='" + remoteIpAddress + '\'' +
                ", remotePort=" + remotePort +
                ", bindParam=" + bindParam +
                ", reconnectInterval=" + reconnectInterval +
                '}';
    }
}
