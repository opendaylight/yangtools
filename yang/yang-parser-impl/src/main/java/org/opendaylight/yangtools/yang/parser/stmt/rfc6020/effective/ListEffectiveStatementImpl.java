package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;

import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class ListEffectiveStatementImpl extends
        AbstractEffectiveDocumentedDataNodeContainer<QName, ListStatement>
        implements ListSchemaNode, DerivableSchemaNode {
    private final QName qname;
    private final SchemaPath path;

    boolean augmenting;
    boolean addedByUses;
    ListSchemaNode original;
    boolean configuration;
    ConstraintDefinition constraints;
    boolean userOrdered;

    ImmutableList<QName> keyDefinition;
    ImmutableSet<AugmentationSchema> augmentations;
    ImmutableList<UnknownSchemaNode> unknownNodes;

    public ListEffectiveStatementImpl(
            StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);
        // :TODO init other fields

        initKeyDefinition();
        initSubstatementCollections();
        initCopyType(ctx);
    }

    private void initCopyType(
            StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {

        TypeOfCopy typeOfCopy = ctx.getTypeOfCopy();
        switch (typeOfCopy) {
        case ADDED_BY_AUGMENTATION:
            augmenting = true;
            original = (ListSchemaNode) ctx.getOriginalCtx().buildEffective();
            break;
        case ADDED_BY_USES:
            addedByUses = true;
            original = (ListSchemaNode) ctx.getOriginalCtx().buildEffective();
            break;
        default:
            break;
        }
    }

    /**
     *
     */
    private void initKeyDefinition() {
        List<QName> keyDefinitionInit = new LinkedList<QName>();
        KeyEffectiveStatementImpl key = firstEffective(KeyEffectiveStatementImpl.class);

        if (key != null) {
            Collection<SchemaNodeIdentifier> keyParts = key.argument();
            for (SchemaNodeIdentifier keyPart : keyParts) {
                keyDefinitionInit.add(keyPart.getLastComponent());
            }
        }

        this.keyDefinition = ImmutableList.copyOf(keyDefinitionInit);
    }

    private void initSubstatementCollections() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<UnknownSchemaNode>();
        Set<AugmentationSchema> augmentationsInit = new HashSet<AugmentationSchema>();

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
            if (effectiveStatement instanceof AugmentationSchema) {
                AugmentationSchema augmentationSchema = (AugmentationSchema) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<QName> getKeyDefinition() {
        return keyDefinition;
    }

    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public Optional<ListSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return constraints;
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return augmentations;
    }

    @Override
    public boolean isUserOrdered() {
        return userOrdered;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qname == null) ? 0 : qname.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ListEffectiveStatementImpl other = (ListEffectiveStatementImpl) obj;
        if (qname == null) {
            if (other.qname != null) {
                return false;
            }
        } else if (!qname.equals(other.qname)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "list " + qname.getLocalName();
    }
}