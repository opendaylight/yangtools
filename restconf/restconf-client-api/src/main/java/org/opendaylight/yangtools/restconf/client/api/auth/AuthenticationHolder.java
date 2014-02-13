package org.opendaylight.yangtools.restconf.client.api.auth;

/**
 * Created by mbobak on 2/14/14.
 */
public interface AuthenticationHolder {

    public RestAuthType getAuthType();
    public String getUserName();
    public String getPassword();
    public boolean authenticationRequired();
}
