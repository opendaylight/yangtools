package org.opendaylight.yang.gen.v1.bug5446.rev151105;

/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 *
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 *
 */
public class IpAddressBinaryBuilder {
    public static IpAddressBinary getDefaultInstance(java.lang.String defaultValue) {
        return new IpAddressBinary(Ipv4AddressBinary.getDefaultInstance(defaultValue));
    }

    public static IpAddressBinary getDefaultInstance(byte[] defaultValue) {
        if (defaultValue.length == 4) {
            return new IpAddressBinary(new Ipv4AddressBinary(defaultValue));
        } else if (defaultValue.length == 16) {
            return new IpAddressBinary(new Ipv6AddressBinary(defaultValue));
        }
        return null;
    }
}
