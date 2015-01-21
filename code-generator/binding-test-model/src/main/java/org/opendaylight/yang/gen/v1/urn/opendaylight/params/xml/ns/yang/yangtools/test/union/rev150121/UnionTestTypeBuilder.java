package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.UnionTestType;


/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 * 
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 * 
 */
public class UnionTestTypeBuilder {

    public static UnionTestType getDefaultInstance(java.lang.String defaultValue) {
        if (defaultValue.length() > 8) {
            return new UnionTestType(new LowestLevel1(defaultValue));
        } else {
            return new UnionTestType(new LowestLevel2(defaultValue));
        }
    }

}
