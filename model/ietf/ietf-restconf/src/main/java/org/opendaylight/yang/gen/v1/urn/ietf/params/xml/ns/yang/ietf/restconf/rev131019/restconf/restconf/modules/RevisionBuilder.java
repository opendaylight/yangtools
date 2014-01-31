package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.restconf.restconf.modules;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.restconf.restconf.modules.Module.Revision;


/**
**/
public class RevisionBuilder {

    public static Revision getDefaultInstance(String defaultValue) {
        if (defaultValue == null || defaultValue.isEmpty()) {
            return new Revision("");
        }
        String revisionIdentifierPattern =  "\\d{4}-\\d{2}-\\d{2}";
        if (defaultValue.matches(revisionIdentifierPattern)) {
            RevisionIdentifier revisionIdentifier = new RevisionIdentifier(defaultValue);
            return new Revision(revisionIdentifier);
        }
        throw new IllegalArgumentException("Cannot create Revision from " + defaultValue);
    }

}
