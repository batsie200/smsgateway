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

import com.cloudhopper.commons.util.LoadBalancedList;
import com.cloudhopper.commons.util.LoadBalancedLists;
import com.cloudhopper.commons.util.RoundRobinLoadBalancedList;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import zw.co.econet.smsgateway.persistence.model.SmppConfiguration;
import zw.co.econet.smsgateway.persistence.services.SmppConfigurationService;
import zw.co.econet.smsgateway.smpp.SmppService;

import javax.annotation.PostConstruct;
import java.util.Optional;


@Service
@Profile("jsmpp")
public class JsmppSmppService implements SmppService {
    private LoadBalancedList<AutoReconnectGateway> balancedList;
    private final ApplicationContext context;

    private final SmppConfigurationService configurationFacade;

    @Autowired
    public JsmppSmppService(SmppConfigurationService configurationFacade, ApplicationContext context) {
        this.configurationFacade = configurationFacade;
        this.context = context;
    }


    @Override
    @PostConstruct
    public void createSmppSession() {
        balancedList = LoadBalancedLists.synchronizedList(new RoundRobinLoadBalancedList<>());
        configurationFacade.findAll().stream().filter(SmppConfiguration::isState).forEach(smppConfiguration ->
                balancedList.set(createClient(smppConfiguration), 1)
        );
    }

    @Override
    public Optional<String> sendSms(String destinationNumber, String sourceNumber, String message) {
        AutoReconnectGateway autoReconnectGateway = balancedList.getNext();
        return Optional.of(autoReconnectGateway.sendSms(destinationNumber, sourceNumber, message));

    }

    private AutoReconnectGateway createClient(SmppConfiguration smppConfiguration) {
        BindType smppBindType = null;
        switch (smppConfiguration.getSmppBindType()) {
            case RECEIVER:
                smppBindType = BindType.BIND_RX;
                break;
            case TRANSMITTER:
                smppBindType = BindType.BIND_TX;
                break;
            case TRANSCEIVER:
                smppBindType = BindType.BIND_TRX;
                break;
            default :
        }
        BindParameter bindParameter = new BindParameter(smppBindType, smppConfiguration.getUsername(), smppConfiguration.getPassword(), "cp", TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, null);
        AutoReconnectGateway autoReconnectGateway = context.getBean(AutoReconnectGateway.class);
        autoReconnectGateway.initialize(smppConfiguration.getHostname(), smppConfiguration.getPort(), bindParameter);
        autoReconnectGateway.startMessageListener(context.getBean( JsmppSmsReceiver.class));
        return autoReconnectGateway;
    }
}
