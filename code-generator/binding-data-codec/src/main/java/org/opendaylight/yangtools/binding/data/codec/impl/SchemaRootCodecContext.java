package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

class SchemaRootCodecContext extends DataContainerCodecContext<SchemaContext> {

    private SchemaRootCodecContext(final CodecContextFactory factory) {
        super(SchemaRootCodecContext.class, null, factory.getRuntimeContext().getSchemaContext(), factory);
    }

    /**
     * Creates RootNode from supplied CodecContextFactory.
     *
     * @param factory CodecContextFactory
     * @return
     */
    static SchemaRootCodecContext create(final CodecContextFactory factory) {
        return new SchemaRootCodecContext(factory);
    }

    @Override
    DataContainerCodecContext<?> loadChild(final Class<?> childClass) {
        Class<Object> parent = org.opendaylight.yangtools.util.ClassLoaderUtils.findFirstGenericArgument(childClass, ChildOf.class);
        Preconditions.checkArgument(DataRoot.class.isAssignableFrom(parent));

        QName qname = BindingReflections.findQName(childClass);
        DataSchemaNode childSchema = getSchema().getDataChildByName(qname);
        return DataContainerCodecContext.from(childClass, childSchema, factory);
    }

    @Override
    public YangInstanceIdentifier.PathArgument getDomPathArgument() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeCodecContext getYangIdentifierChild(final YangInstanceIdentifier.PathArgument arg) {

        QName childQName = arg.getNodeType();
        DataSchemaNode childSchema = schema.getDataChildByName(childQName);
        Preconditions.checkArgument(childSchema != null, "Argument %s is not valid child of %s", arg, schema);
        if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceNode) {
            Class<?> childCls = factory.getRuntimeContext().getClassForSchema(childSchema);
            DataContainerCodecContext<?> childNode = getStreamChild(childCls);
            return childNode;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}