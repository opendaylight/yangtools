package org.opendaylight.yangtools.yang.data.util.tree;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class MetadataFactory<M> {

    abstract @Nullable M valueFor(PathArgument arg,DataSchemaNode node);

    abstract @Nullable M valueFor(PathArgument arg,AugmentationSchema schema);
}
