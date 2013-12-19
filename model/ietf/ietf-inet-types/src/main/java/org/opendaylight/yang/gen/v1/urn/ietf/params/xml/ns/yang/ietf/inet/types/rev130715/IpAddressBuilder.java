package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import com.google.common.net.InetAddresses;


/**
**/
public final class IpAddressBuilder {
    private IpAddressBuilder() {
    }

    public static IpAddress getDefaultInstance(String defaultValue) {
        final InetAddress a = InetAddresses.forString(defaultValue);
        if (a instanceof Inet4Address) {
            return new IpAddress(new Ipv4Address(defaultValue));
        } else if (a instanceof Inet6Address) {
            return new IpAddress(new Ipv6Address(defaultValue));
        } else {
            throw new IllegalArgumentException("Unsupported address type " + a.getClass());
        }
    }
}
