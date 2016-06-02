package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.IdentBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.IdentOne;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.IdentTwo;

/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 *
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 *
 */
public class UnionTypeBuilder {

    public static UnionType getDefaultInstance(java.lang.String defaultValue) {
        Class<? extends IdentBase> identityref = null;
        if (defaultValue.equals("IdentOne")) {
            return new UnionType(IdentOne.class);
        }
        if (defaultValue.equals("IdentTwo")) {
            return new UnionType(IdentTwo.class);
        }
        throw new IllegalArgumentException("Unknown UnionType string " + defaultValue);
    }

}
