package zw.co.econet.smsgateway.persistence.services.implementation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zw.co.econet.smsgateway.persistence.model.SystemRole;
import zw.co.econet.smsgateway.persistence.repositories.SystemRoleRepository;
import zw.co.econet.smsgateway.persistence.services.SystemRoleService;

@Repository
@Transactional
@Slf4j
public class SystemRoleServiceImpl implements SystemRoleService {

    private final SystemRoleRepository repository;

    @Autowired
    public SystemRoleServiceImpl(SystemRoleRepository repository) {
        this.repository = repository;
    }


    @Override
    public SystemRole save(SystemRole systemRole) {
        return repository.save(systemRole);
    }

    @Override
    public void delete(SystemRole systemRole) {
        repository.delete(systemRole);
    }

    @Override
    public SystemRole findById(String id) {
        return repository.findOne(id);
    }
}
