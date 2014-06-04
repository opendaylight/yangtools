package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;


public interface ExtensionBuilder extends SchemaNodeBuilder {

    void setArgument(String argument);

    void setYinElement(boolean yin);

    @Override
    ExtensionDefinition build();

}