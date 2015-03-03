package org.opendaylight.yangtools.binding.data.codec.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
*
* Navigable tree representing hierarchy of Binding to Normalized Node codecs
*
* This navigable tree is associated to conrete set of YANG models,
* represented by SchemaContext and provides access to  subtree specific
* serialization context.
*
* TODO: Add more detailed documentation
**/
interface BindingCodecTree {

      @Nonnull SchemaContext getSchemaContext();

      @Nullable <T extends DataObject> BindingCodecTreeNode<T> getSubtreeCodec(InstanceIdentifier<T> path);

      @Nullable BindingCodecTreeNode<?> getSubtreeCodec(YangInstanceIdentifier path);

      @Nullable BindingCodecTreeNode<?> getSubtreeCodec(SchemaPath path);

}