package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class ListEffectiveStatementImpl extends AbstractEffectiveDocumentedDataNodeContainer<QName, ListStatement>
        implements ListSchemaNode, DerivableSchemaNode {
    private final QName qname;
    private final SchemaPath path;

    boolean augmenting;
    private boolean addedByUses;
    ListSchemaNode original;
    boolean configuration = true;
    ConstraintDefinition constraints;
    boolean userOrdered;

    ImmutableList<QName> keyDefinition;
    ImmutableSet<AugmentationSchema> augmentations;
    ImmutableList<UnknownSchemaNode> unknownNodes;

    public ListEffectiveStatementImpl(StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);
        this.constraints = new EffectiveConstraintDefinitionImpl(this);

        initSubstatementCollectionsAndFields();
        initCopyType(ctx);

        // should be after initSubstatementCollectionsAndFields()
        initKeyDefinition(ctx);
    }

    private void initCopyType(
            StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {

        List<TypeOfCopy> copyTypesFromOriginal = ctx.getCopyHistory();

        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_AUGMENTATION)) {
            augmenting = true;
        }
        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES)) {
            addedByUses = true;
        }
        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES_AUGMENTATION)) {
            addedByUses = augmenting = true;
        }

        if (ctx.getOriginalCtx() != null) {
            original = (ListSchemaNode) ctx.getOriginalCtx().buildEffective();
        }
    }

    private void initKeyDefinition(StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {
        List<QName> keyDefinitionInit = new LinkedList<>();
        KeyEffectiveStatementImpl keyEffectiveSubstatement = firstEffective(KeyEffectiveStatementImpl.class);

        if (keyEffectiveSubstatement != null) {
            Set<QName> possibleLeafQNamesForKey = new HashSet<>();

            for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
                if (effectiveStatement instanceof LeafSchemaNode) {
                    possibleLeafQNamesForKey.add(((LeafSchemaNode) effectiveStatement).getQName());
                }
            }

            Collection<SchemaNodeIdentifier> keys = keyEffectiveSubstatement.argument();
            for (SchemaNodeIdentifier key : keys) {
                final QName keyQName = key.getLastComponent();

                if (!possibleLeafQNamesForKey.contains(keyQName)) {
                    throw new IllegalArgumentException(String.format("Key '%s' misses node '%s' in list '%s', file %s",
                            keyEffectiveSubstatement.getDeclared().rawArgument(), keyQName.getLocalName(), ctx.getStatementArgument(),
                            ctx.getStatementSourceReference()));
                }

                keyDefinitionInit.add(keyQName);
            }
        }

        this.keyDefinition = ImmutableList.copyOf(keyDefinitionInit);
    }

    private void initSubstatementCollectionsAndFields() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();
        Set<AugmentationSchema> augmentationsInit = new HashSet<>();

        boolean configurationInit = false;
        boolean userOrderedInit = false;
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
            if (effectiveStatement instanceof AugmentationSchema) {
                AugmentationSchema augmentationSchema = (AugmentationSchema) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
            if (!configurationInit && effectiveStatement instanceof ConfigEffectiveStatementImpl) {
                ConfigEffectiveStatementImpl configStmt = (ConfigEffectiveStatementImpl) effectiveStatement;
                this.configuration = configStmt.argument();
                configurationInit = true;
            }
            if (!userOrderedInit && effectiveStatement instanceof OrderedByEffectiveStatementImpl) {
                OrderedByEffectiveStatementImpl orderedByStmt = (OrderedByEffectiveStatementImpl) effectiveStatement;
                this.userOrdered = orderedByStmt.argument().equals("user") ? true : false;
                userOrderedInit = true;
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