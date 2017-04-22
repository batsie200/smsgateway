package zw.co.econet.smsgateway.persistence.services.implementation;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import zw.co.econet.smsgateway.persistence.model.Sms;
import zw.co.econet.smsgateway.persistence.repositories.SmsRepository;
import zw.co.econet.smsgateway.persistence.services.SmsService;
import zw.co.econet.smsgateway.util.MessageState;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by oswin on 8/12/2016.
 */
@Repository
@Transactional
public class SmsServiceImpl implements SmsService {
    private final SmsRepository repository;

    public SmsServiceImpl(SmsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Sms findByServerReference(String submitId) {
        return repository.findByServerReference(submitId);
    }

    @Override
    public Sms findbySubmitId(String submitId) {
        return repository.findBySubmitId(submitId);
    }

    @Override
    public Sms findByServerReferenceAndSender(String submitId, String sender) {
        return repository.findByServerReferenceAndSender(submitId, sender);
    }

    @Override
    public List<Sms> findApplicationMessagesByState(MessageState state, String sender, int resultsSize) {
        return repository.findPendingBatch(state, sender, new PageRequest(1, resultsSize));
    }

    @Override
    public void save(Sms sms) {
        repository.save(sms);
    }

    @Override
    public void save(List<Sms> smsList) {
        repository.save(smsList);
    }
}
