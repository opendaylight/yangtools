package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class MissingSchemaException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    protected MissingSchemaException(final String msg) {
        super(msg);
    }

    private static MissingSchemaException create(final String format, final Object... args) {
        return new MissingSchemaException(String.format(format, args));
    }

    static void checkModulePresent(final SchemaContext schemaContext, final QName name) {
        if(schemaContext.findModuleByNamespaceAndRevision(name.getNamespace(), name.getRevision()) == null) {
            throw MissingSchemaException.create("Module %s is not present in current schema context.",name.getModule());
        }
    }

    static void checkModulePresent(final SchemaContext schemaContext, final YangInstanceIdentifier.PathArgument child) {
        checkModulePresent(schemaContext, extractName(child));
    }

    private static QName extractName(final PathArgument child) {
        if(child instanceof YangInstanceIdentifier.AugmentationIdentifier) {
            final Set<QName> children = ((YangInstanceIdentifier.AugmentationIdentifier) child).getPossibleChildNames();
            Preconditions.checkArgument(!children.isEmpty(),"Augmentation without childs must not be used in data");
            return children.iterator().next();
        }
        return child.getNodeType();
    }




}
