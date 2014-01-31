package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.DatastoreIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.DatastoreIdentifier.Enumeration;


/**
**/
public class DatastoreIdentifierBuilder {

    public static DatastoreIdentifier getDefaultInstance(String defaultValue) {
        if (defaultValue == null) {
            throw new IllegalArgumentException("Cannot create DatastoreIdentifier from " + defaultValue);
        }
        if (defaultValue.equals("candidate")) {
            return new DatastoreIdentifier(Enumeration.Candidate);
        }
        if (defaultValue.equals("running")) {
            return new DatastoreIdentifier(Enumeration.Running);
        }
        if (defaultValue.equals("startup")) {
            return new DatastoreIdentifier(Enumeration.Startup);
        }
        return new DatastoreIdentifier(defaultValue);
    }

}
