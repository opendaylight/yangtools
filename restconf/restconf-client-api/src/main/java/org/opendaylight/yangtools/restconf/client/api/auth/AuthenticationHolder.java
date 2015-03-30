package org.opendaylight.yangtools.restconf.client.api.auth;

/**
 * Created by mbobak on 2/14/14.
 */
public interface AuthenticationHolder {

    RestAuthType getAuthType();
    String getUserName();
    String getPassword();
    boolean authenticationRequired();
}
