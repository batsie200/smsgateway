package zw.co.econet.smsgateway.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.co.econet.smsgateway.persistence.model.SystemRole;

/**
 * Created by oswin on 19/12/2016.
 */
public interface SystemRoleRepository extends JpaRepository<SystemRole, String> {
}
