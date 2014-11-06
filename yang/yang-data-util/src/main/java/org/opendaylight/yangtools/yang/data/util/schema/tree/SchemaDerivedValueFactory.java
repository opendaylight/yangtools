package org.opendaylight.yangtools.yang.data.util.schema.tree;

import javax.annotation.Nullable;

import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.common.annotations.Beta;

@Beta
public interface SchemaDerivedValueFactory<V> {

    V from(SchemaContext ctx);

    @Nullable V from(DataSchemaNode node);

    @Nullable V from(AugmentationSchema schema);
}