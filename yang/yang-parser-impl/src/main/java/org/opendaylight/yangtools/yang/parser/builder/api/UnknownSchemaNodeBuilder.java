package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public interface UnknownSchemaNodeBuilder extends SchemaNodeBuilder, DocumentedNodeBuilder {

     @Override
    SchemaPath getPath();

     boolean isAddedByUses();

     void setAddedByUses(boolean addedByUses);

     QName getNodeType();

     String getNodeParameter();

     void setNodeParameter(String nodeParameter);

     ExtensionDefinition getExtensionDefinition();

     void setExtensionDefinition(ExtensionDefinition extensionDefinition);

     ExtensionBuilder getExtensionBuilder();

     void setExtensionBuilder(ExtensionBuilder extension);

     @Override
    UnknownSchemaNode build();

    void setNodeType(QName qName);

}