package org.opendaylight.yangtools.yang.model.api.stmt;

public interface ConditionalDataDefinition extends ConditionalFeature {

    WhenStatement getWhenStatement();

}
