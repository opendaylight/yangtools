package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import com.google.common.net.InetAddresses;


/**
**/
public final class IpAddressNoZoneBuilder {
    private IpAddressNoZoneBuilder() {

    }

    public static IpAddressNoZone getDefaultInstance(String defaultValue) {
        final InetAddress a = InetAddresses.forString(defaultValue);
        if (a instanceof Inet4Address) {
            return new IpAddressNoZone(new Ipv4AddressNoZone(defaultValue));
        } else if (a instanceof Inet6Address) {
            return new IpAddressNoZone(new Ipv6AddressNoZone(defaultValue));
        } else {
            throw new IllegalArgumentException("Unhandled address type " + a.getClass());
        }
    }
}
