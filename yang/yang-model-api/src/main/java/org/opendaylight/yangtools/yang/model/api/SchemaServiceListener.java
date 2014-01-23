package org.opendaylight.yangtools.yang.model.api;

import java.util.EventListener;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;

// TODO rename to schemaContextListener
public interface SchemaServiceListener extends EventListener {

    void onGlobalContextUpdated(SchemaContext context);

}
