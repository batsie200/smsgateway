package zw.co.econet.smsgateway.persistence.services;


import zw.co.econet.smsgateway.persistence.model.SystemRole;

/**
 * Created by oswin on 19/12/2016.
 */
public interface SystemRoleService {
    SystemRole save(SystemRole systemRole);
    void delete(SystemRole systemRole);
    SystemRole findById(String id);
}