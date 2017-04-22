package zw.co.econet.smsgateway.persistence.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zw.co.econet.smsgateway.persistence.model.Sms;
import zw.co.econet.smsgateway.util.MessageState;

import java.util.List;

/**
 * Created by oswin on 8/12/2016.
 */
public interface SmsRepository extends JpaRepository<Sms, Long> {
    Sms findByServerReference(String serverReference);

    Sms findByServerReferenceAndSender(String submitId, String sender);

    Sms findBySubmitId(String submitId);

    @Query("select s from Sms s where s.collectionState =:collectionState and s.sender= :sender")
    List<Sms> findPendingBatch(@Param("collectionState") MessageState collectionState, @Param("sender") String sender, Pageable pageable);
}
