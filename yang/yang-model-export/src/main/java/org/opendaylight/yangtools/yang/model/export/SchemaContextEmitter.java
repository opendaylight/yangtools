/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;

@Beta
@NotThreadSafe
class SchemaContextEmitter {

    private final YangModuleWriter writer;
    private final boolean emitInstantiated;
    private final boolean emitUses;
    private final Map<QName, StatementDefinition> extensions;
    private final YangVersion yangVersion;

    SchemaContextEmitter(final YangModuleWriter writer, final Map<QName, StatementDefinition> extensions,
            final YangVersion yangVersion) {
        this(writer, extensions, yangVersion, false, true);
    }

    SchemaContextEmitter(final YangModuleWriter writer, final Map<QName, StatementDefinition> extensions,
            final YangVersion yangVersion, final boolean emitInstantiated, final boolean emitUses) {
        this.writer = Preconditions.checkNotNull(writer);
        this.emitInstantiated = emitInstantiated;
        this.emitUses = emitUses;
        this.extensions = Preconditions.checkNotNull(extensions);
        this.yangVersion = yangVersion;
    }

    static void writeToStatementWriter(final Module module, final SchemaContext ctx,
            final StatementTextWriter statementWriter) {
        final YangModuleWriter yangSchemaWriter = SchemaToStatementWriterAdaptor.from(statementWriter);
        final Map<QName, StatementDefinition> extensions = ExtensionStatement.mapFrom(ctx.getExtensions());
        new SchemaContextEmitter(yangSchemaWriter, extensions, YangVersion.parse(module.getYangVersion()).orElse(null)).emitModule(module);
    }

    void emitModule(final Module input) {
        writer.startModuleNode(input.getName());
        emitModuleHeader(input);
        emitLinkageNodes(input);
        emitMetaNodes(input);
        emitRevisionNodes(input);
        emitBodyNodes(input);
        writer.endNode();
    }

    private void emitModuleHeader(final Module input) {
        emitYangVersionNode(input.getYangVersion());
        emitNamespace(input.getNamespace());
        emitPrefixNode(input.getPrefix());
    }

    @SuppressWarnings("unused")
    private void emitSubmodule(final String input) {
        /*
         * FIXME: BUG-2444:  Implement submodule export
         *
         * submoduleHeaderNodes linkageNodes metaNodes revisionNodes bodyNodes
         * writer.endNode();
         */
    }

    @SuppressWarnings("unused")
    private void emitSubmoduleHeaderNodes(final Module input) {
        /*
         * FIXME: BUG-2444:  Implement submodule headers properly
         *
         * :yangVersionNode //Optional
         *
         * :belongsToNode
         */
    }

    private void emitMetaNodes(final Module input) {
        emitOrganizationNode(input.getOrganization());
        emitContact(input.getContact());
        emitDescriptionNode(input.getDescription());
        emitReferenceNode(input.getReference());
    }

    private void emitLinkageNodes(final Module input) {
        for (final ModuleImport importNode : input.getImports()) {
            emitImport(importNode);
        }
        /*
         * FIXME: BUG-2444:  Emit include statements
         */
    }

    private void emitRevisionNodes(final Module input) {
        /*
         * FIXME: BUG-2444:  emit revisions properly, when parsed model will provide enough
         * information
         */
        emitRevision(input.getRevision());

    }

    private void emitBodyNodes(final Module input) {

        for (final ExtensionDefinition extension : input.getExtensionSchemaNodes()) {
            emitExtension(extension);
        }
        for (final FeatureDefinition definition : input.getFeatures()) {
            emitFeature(definition);
        }
        for (final IdentitySchemaNode identity : input.getIdentities()) {
            emitIdentity(identity);
        }
        for (final Deviation deviation : input.getDeviations()) {
            emitDeviation(deviation);
        }

        emitDataNodeContainer(input);

        for (final AugmentationSchema augmentation : input.getAugmentations()) {
            emitAugment(augmentation);
        }
        for (final RpcDefinition rpc : input.getRpcs()) {
            emitRpc(rpc);
        }

        emitNotifications(input.getNotifications());
    }

    private void emitDataNodeContainer(final DataNodeContainer input) {
        for (final TypeDefinition<?> typedef : input.getTypeDefinitions()) {
            emitTypedefNode(typedef);
        }
        for (final GroupingDefinition grouping : input.getGroupings()) {
            emitGrouping(grouping);
        }
        for (final DataSchemaNode child : input.getChildNodes()) {
            emitDataSchemaNode(child);
        }
        for (final UsesNode usesNode : input.getUses()) {
            emitUsesNode(usesNode);
        }
    }

    private void emitDataSchemaNode(final DataSchemaNode child) {
        if (!emitInstantiated && (child.isAddedByUses() || child.isAugmenting())) {
            // We skip instantiated nodes.
            return;
        }

        if (child instanceof ContainerSchemaNode) {
            emitContainer((ContainerSchemaNode) child);
        } else if (child instanceof LeafSchemaNode) {
            emitLeaf((LeafSchemaNode) child);
        } else if (child instanceof LeafListSchemaNode) {
            emitLeafList((LeafListSchemaNode) child);
        } else if (child instanceof ListSchemaNode) {
            emitList((ListSchemaNode) child);
        } else if (child instanceof ChoiceSchemaNode) {
            emitChoice((ChoiceSchemaNode) child);
        } else if (child instanceof AnyXmlSchemaNode) {
            emitAnyxml((AnyXmlSchemaNode) child);
        } else if (child instanceof AnyDataSchemaNode) {
            emitAnydata((AnyDataSchemaNode) child);
        } else {
            throw new UnsupportedOperationException("Not supported DataSchemaNode type " + child.getClass());
        }
    }

    private void emitYangVersionNode(final String input) {
        writer.startYangVersionNode(input);
        writer.endNode();
    }

    private void emitImport(final ModuleImport importNode) {
        writer.startImportNode(importNode.getModuleName());
        emitDescriptionNode(importNode.getDescription());
        emitReferenceNode(importNode.getReference());
        emitPrefixNode(importNode.getPrefix());
        emitRevisionDateNode(importNode.getRevision());
        writer.endNode();
    }

    @SuppressWarnings("unused")
    private void emitInclude(final String input) {
        /*
         * FIXME: BUG-2444:  Implement proper export of include statements
         * startIncludeNode(IdentifierHelper.getIdentifier(String :input));
         *
         *
         * :revisionDateNode :writer.endNode();)
         */
    }

    private void emitNamespace(final URI uri) {
        writer.startNamespaceNode(uri);
        writer.endNode();

    }

    private void emitPrefixNode(final String input) {
        writer.startPrefixNode(input);
        writer.endNode();

    }

    @SuppressWarnings("unused")
    private void emitBelongsTo(final String input) {
        /*
         * FIXME: BUG-2444:  Implement proper export of belongs-to statements
         * startIncludeNode(IdentifierHelper.getIdentifier(String :input));
         *
         *
         * :writer.startBelongsToNode(IdentifierHelper.getIdentifier(String
         * :input));
         *
         *
         * :prefixNode
         * :writer.endNode();
         *
         */

    }

    private void emitOrganizationNode(final String input) {
        if (!Strings.isNullOrEmpty(input)) {
            writer.startOrganizationNode(input);
            writer.endNode();
        }
    }

    private void emitContact(final String input) {
        if (!Strings.isNullOrEmpty(input)) {
            writer.startContactNode(input);
            writer.endNode();
        }
    }

    private void emitDescriptionNode(@Nullable final String input) {
        if (!Strings.isNullOrEmpty(input)) {
            writer.startDescriptionNode(input);
            writer.endNode();
        }
    }

    private void emitReferenceNode(@Nullable final String input) {
        if (!Strings.isNullOrEmpty(input)) {
            writer.startReferenceNode(input);
            writer.endNode();
        }
    }

    private void emitUnitsNode(@Nullable final String input) {
        if (!Strings.isNullOrEmpty(input)) {
            writer.startUnitsNode(input);
            writer.endNode();
        }
    }

    private void emitRevision(final Date date) {
        writer.startRevisionNode(date);

        //
        // FIXME: BUG-2444: FIXME: BUG-2444: BUG-2417: descriptionNode //FIXME: BUG-2444: Optional
        // FIXME: BUG-2444: FIXME: BUG-2444: BUG-2417: referenceNode //FIXME: BUG-2444: Optional
        writer.endNode();

    }

    private void emitRevisionDateNode(@Nullable final Date date) {
        if (date != null) {
            writer.startRevisionDateNode(date);
            writer.endNode();
        }
    }

    private void emitExtension(final ExtensionDefinition extension) {
        writer.startExtensionNode(extension.getQName());
        emitArgument(extension.getArgument(),extension.isYinElement());
        emitStatusNode(extension.getStatus());
        emitDescriptionNode(extension.getDescription());
        emitReferenceNode(extension.getReference());
        emitUnknownStatementNodes(extension.getUnknownSchemaNodes());
        writer.endNode();

    }

    private void emitArgument(final @Nullable String input, final boolean yinElement) {
        if (input != null) {
            writer.startArgumentNode(input);
            emitYinElement(yinElement);
            writer.endNode();
        }

    }

    private void emitYinElement(final boolean yinElement) {
        writer.startYinElementNode(yinElement);
        writer.endNode();

    }

    private void emitIdentity(final IdentitySchemaNode identity) {
        writer.startIdentityNode(identity.getQName());
        emitBaseIdentities(identity.getBaseIdentities());
        emitStatusNode(identity.getStatus());
        emitDescriptionNode(identity.getDescription());
        emitReferenceNode(identity.getReference());
        writer.endNode();
    }

    private void emitBaseIdentities(final Set<IdentitySchemaNode> identities) {
        for (final IdentitySchemaNode identitySchemaNode : identities) {
            emitBase(identitySchemaNode.getQName());
        }
    }

    private void emitBase(final QName qName) {
        writer.startBaseNode(qName);
        writer.endNode();
    }

    private void emitFeature(final FeatureDefinition definition) {
        writer.startFeatureNode(definition.getQName());

        // FIXME: BUG-2444: FIXME: BUG-2444:  Expose ifFeature *(ifFeatureNode )
        emitStatusNode(definition.getStatus());
        emitDescriptionNode(definition.getDescription());
        emitReferenceNode(definition.getReference());
        writer.endNode();

    }

    @SuppressWarnings("unused")
    private void emitIfFeature(final String input) {
        /*
         * FIXME: BUG-2444:  Implement proper export of include statements
         * startIncludeNode(IdentifierHelper.getIdentifier(String :input));
         *
         */
    }

    private void emitTypedefNode(final TypeDefinition<?> typedef) {
        writer.startTypedefNode(typedef.getQName());
        // Differentiate between derived type and existing type
        // name.
        emitTypeNodeDerived(typedef);
        emitUnitsNode(typedef.getUnits());
        emitDefaultNode(typedef.getDefaultValue());
        emitStatusNode(typedef.getStatus());
        emitDescriptionNode(typedef.getDescription());
        emitReferenceNode(typedef.getReference());
        emitUnknownStatementNodes(typedef.getUnknownSchemaNodes());
        writer.endNode();

    }

    private void emitTypeNode(final SchemaPath parentPath, final TypeDefinition<?> subtype) {
        final SchemaPath path = subtype.getPath();
        if (isPrefix(parentPath.getPathFromRoot(), path.getPathFromRoot())) {
            emitTypeNodeDerived(subtype);
        } else {
            emitTypeNodeReferenced(subtype);
        }
    }

    private void emitTypeNodeReferenced(final TypeDefinition<?> typeDefinition) {
        writer.startTypeNode(typeDefinition.getQName());
        writer.endNode();

    }

    private void emitTypeNodeDerived(final TypeDefinition<?> typeDefinition) {
        final TypeDefinition<?> b = typeDefinition.getBaseType();
        final TypeDefinition<?> baseType = b == null ? typeDefinition : b;
        writer.startTypeNode(baseType.getQName());
        emitTypeBodyNodes(typeDefinition);
        writer.endNode();

    }

    private void emitTypeBodyNodes(final TypeDefinition<?> typeDef) {
        if (typeDef instanceof UnsignedIntegerTypeDefinition) {
            emitUnsignedIntegerSpecification((UnsignedIntegerTypeDefinition) typeDef);
        } else if (typeDef instanceof IntegerTypeDefinition) {
            emitIntegerSpefication((IntegerTypeDefinition) typeDef);
        } else if (typeDef instanceof DecimalTypeDefinition) {
            emitDecimal64Specification((DecimalTypeDefinition) typeDef);
        } else if (typeDef instanceof StringTypeDefinition) {
            emitStringRestrictions((StringTypeDefinition) typeDef);
        } else if (typeDef instanceof EnumTypeDefinition) {
            emitEnumSpecification((EnumTypeDefinition) typeDef);
        } else if (typeDef instanceof LeafrefTypeDefinition) {
            emitLeafrefSpecification((LeafrefTypeDefinition) typeDef);
        } else if (typeDef instanceof IdentityrefTypeDefinition) {
            emitIdentityrefSpecification((IdentityrefTypeDefinition) typeDef);
        } else if (typeDef instanceof InstanceIdentifierTypeDefinition) {
            emitInstanceIdentifierSpecification((InstanceIdentifierTypeDefinition) typeDef);
        } else if (typeDef instanceof BitsTypeDefinition) {
            emitBitsSpecification((BitsTypeDefinition) typeDef);
        } else if (typeDef instanceof UnionTypeDefinition) {
            emitUnionSpecification((UnionTypeDefinition) typeDef);
        } else if (typeDef instanceof BinaryTypeDefinition) {
            emitLength(((BinaryTypeDefinition) typeDef).getLengthConstraints());
        } else if (typeDef instanceof BooleanTypeDefinition || typeDef instanceof EmptyTypeDefinition) {
            // NOOP
        } else {
            throw new IllegalArgumentException("Not supported type " + typeDef.getClass());
        }
    }

    private void emitIntegerSpefication(final IntegerTypeDefinition typeDef) {
        emitRangeNodeOptional(typeDef.getRangeConstraints());
    }

    private void emitUnsignedIntegerSpecification(final UnsignedIntegerTypeDefinition typeDef) {
        emitRangeNodeOptional(typeDef.getRangeConstraints());

    }

    private void emitRangeNodeOptional(final List<RangeConstraint> list) {
        // FIXME: BUG-2444:  Wrong decomposition in API, should be LenghtConstraint
        // which contains ranges.
        if (!list.isEmpty()) {
            writer.startRangeNode(toRangeString(list));
            final RangeConstraint first = list.iterator().next();
            emitErrorMessageNode(first.getErrorMessage());
            emitErrorAppTagNode(first.getErrorAppTag());
            emitDescriptionNode(first.getDescription());
            emitReferenceNode(first.getReference());
            writer.endNode();
        }

    }

    private void emitDecimal64Specification(final DecimalTypeDefinition typeDefinition) {
        emitFranctionDigitsNode(typeDefinition.getFractionDigits());
        emitRangeNodeOptional(typeDefinition.getRangeConstraints());

    }

    private void emitFranctionDigitsNode(final Integer fractionDigits) {
        writer.startFractionDigitsNode(fractionDigits);
        writer.endNode();
    }

    private void emitStringRestrictions(final StringTypeDefinition typeDef) {

        // FIXME: BUG-2444:  Wrong decomposition in API, should be LenghtConstraint
        // which contains ranges.
        emitLength(typeDef.getLengthConstraints());

        for (final PatternConstraint pattern : typeDef.getPatternConstraints()) {
            emitPatternNode(pattern);
        }

    }

    private void emitLength(final List<LengthConstraint> list) {
        if (!list.isEmpty()) {
            writer.startLengthNode(toLengthString(list));
            // FIXME: BUG-2444:  Workaround for incorrect decomposition in API
            final LengthConstraint first = list.iterator().next();
            emitErrorMessageNode(first.getErrorMessage());
            emitErrorAppTagNode(first.getErrorAppTag());
            emitDescriptionNode(first.getDescription());
            emitReferenceNode(first.getReference());
            writer.endNode();
        }
    }

    private static String toLengthString(final List<LengthConstraint> list) {
        final Iterator<LengthConstraint> it = list.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        boolean haveNext;
        do {
            final LengthConstraint current = it.next();
            haveNext = it.hasNext();
            appendRange(sb, current.getMin(), current.getMax(), haveNext);
        } while (haveNext);

        return sb.toString();
    }

    private static String toRangeString(final List<RangeConstraint> list) {
        final Iterator<RangeConstraint> it = list.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        boolean haveNext;
        do {
            final RangeConstraint current = it.next();
            haveNext = it.hasNext();
            appendRange(sb, current.getMin(), current.getMax(), haveNext);
        } while (haveNext);

        return sb.toString();
    }

    private static void appendRange(final StringBuilder sb, final Number min, final Number max,
            final boolean haveNext) {
        sb.append(min);
        if (!min.equals(max)) {
            sb.append("..");
            sb.append(max);
        }
        if (haveNext) {
            sb.append('|');
        }
    }

    private void emitPatternNode(final PatternConstraint pattern) {
        writer.startPatternNode(pattern.getRawRegularExpression());
        // FIXME: BUG-2444: Optional
        emitErrorMessageNode(pattern.getErrorMessage());
        // FIXME: BUG-2444: Optional
        emitErrorAppTagNode(pattern.getErrorAppTag());
        emitDescriptionNode(pattern.getDescription());
        emitModifier(pattern.getModifier());
        writer.endNode();
    }

    private void emitModifier(final ModifierKind modifier) {
        if(modifier != null) {
            writer.startModifierNode(modifier);
            writer.endNode();
        }
    }

    private void emitDefaultNodes(final Collection<String> defaults) {
        for (final String defaultValue : defaults) {
            emitDefaultNode(defaultValue);
        }
    }

    private void emitDefaultNode(@Nullable final Object object) {
        if (object != null) {
            writer.startDefaultNode(object.toString());
            writer.endNode();
        }
    }

    private void emitEnumSpecification(final EnumTypeDefinition typeDefinition) {
        for (final EnumPair enumValue : typeDefinition.getValues()) {
            emitEnumNode(enumValue);
        }
    }

    private void emitEnumNode(final EnumPair enumValue) {
        writer.startEnumNode(enumValue.getName());
        emitValueNode(enumValue.getValue());
        emitStatusNode(enumValue.getStatus());
        emitDescriptionNode(enumValue.getDescription());
        emitReferenceNode(enumValue.getReference());
        writer.endNode();
    }

    private void emitLeafrefSpecification(final LeafrefTypeDefinition typeDefinition) {
        emitPathNode(typeDefinition.getPathStatement());
        if (YangVersion.VERSION_1_1 == yangVersion) {
            emitRequireInstanceNode(typeDefinition.requireInstance());
        }
    }

    private void emitPathNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startPathNode(revisionAwareXPath);
        writer.endNode();
    }

    private void emitRequireInstanceNode(final boolean require) {
        writer.startRequireInstanceNode(require);
        writer.endNode();
    }

    private void emitInstanceIdentifierSpecification(final InstanceIdentifierTypeDefinition typeDefinition) {
        emitRequireInstanceNode(typeDefinition.requireInstance());
    }

    private void emitIdentityrefSpecification(final IdentityrefTypeDefinition typeDefinition) {
        emitBaseIdentities(typeDefinition.getIdentities());
    }

    private void emitUnionSpecification(final UnionTypeDefinition typeDefinition) {
        for (final TypeDefinition<?> subtype : typeDefinition.getTypes()) {
            // FIXME: BUG-2444:  What if we have locally modified types here?
            // is solution to look-up in schema path?
            emitTypeNode(typeDefinition.getPath(), subtype);
        }
    }

    private void emitBitsSpecification(final BitsTypeDefinition typeDefinition) {
        for (final Bit bit : typeDefinition.getBits()) {
            emitBit(bit);
        }
    }

    private void emitBit(final Bit bit) {
        writer.startBitNode(bit.getName());
        emitPositionNode(bit.getPosition());
        emitStatusNode(bit.getStatus());
        emitDescriptionNode(bit.getDescription());
        emitReferenceNode(bit.getReference());
        writer.endNode();
    }

    private void emitPositionNode(@Nullable final Long position) {
        if (position != null) {
            writer.startPositionNode(UnsignedInteger.valueOf(position));
            writer.endNode();
        }
    }

    private void emitStatusNode(@Nullable final Status status) {
        if (status != null) {
            writer.startStatusNode(status);
            writer.endNode();
        }
    }

    private void emitConfigNode(final boolean config) {
        writer.startConfigNode(config);
        writer.endNode();
    }

    private void emitMandatoryNode(final boolean mandatory) {
        writer.startMandatoryNode(mandatory);
        writer.endNode();
    }

    private void emitPresenceNode(final boolean presence) {
        writer.startPresenceNode(presence);
        writer.endNode();
    }

    private void emitOrderedBy(final boolean userOrdered) {
        if (userOrdered) {
            writer.startOrderedByNode("user");
        } else {
            writer.startOrderedByNode("system");
        }
        writer.endNode();
    }

    private void emitMust(@Nullable final MustDefinition mustCondition) {
        if (mustCondition != null && mustCondition.getXpath() != null) {
            writer.startMustNode(mustCondition.getXpath());
            emitErrorMessageNode(mustCondition.getErrorMessage());
            emitErrorAppTagNode(mustCondition.getErrorAppTag());
            emitDescriptionNode(mustCondition.getDescription());
            emitReferenceNode(mustCondition.getReference());
            writer.endNode();
        }

    }

    private void emitErrorMessageNode(@Nullable final String input) {
        if (input != null && !input.isEmpty()) {
            writer.startErrorMessageNode(input);
            writer.endNode();
        }
    }

    private void emitErrorAppTagNode(final String input) {
        if (input != null && !input.isEmpty()) {
            writer.startErrorAppTagNode(input);
            writer.endNode();
        }
    }

    private void emitMinElementsNode(final Integer min) {
        if (min != null) {
            writer.startMinElementsNode(min);
            writer.endNode();
        }
    }

    private void emitMaxElementsNode(final Integer max) {
        if (max != null) {
            writer.startMaxElementsNode(max);
            writer.endNode();
        }
    }

    private void emitValueNode(@Nullable final Integer value) {
        if (value != null) {
            writer.startValueNode(value);
            writer.endNode();
        }
    }

    private void emitDocumentedNode(final DocumentedNode.WithStatus input) {
        emitStatusNode(input.getStatus());
        emitDescriptionNode(input.getDescription());
        emitReferenceNode(input.getReference());
    }

    private void emitGrouping(final GroupingDefinition grouping) {
        writer.startGroupingNode(grouping.getQName());
        emitDocumentedNode(grouping);
        emitDataNodeContainer(grouping);
        emitUnknownStatementNodes(grouping.getUnknownSchemaNodes());
        emitNotifications(grouping.getNotifications());
        emitActions(grouping.getActions());
        writer.endNode();

    }

    private void emitContainer(final ContainerSchemaNode child) {
        writer.startContainerNode(child.getQName());

        //

        emitConstraints(child.getConstraints());
        // FIXME: BUG-2444: whenNode //:Optional
        // FIXME: BUG-2444: *(ifFeatureNode )
        emitPresenceNode(child.isPresenceContainer());
        emitConfigNode(child.isConfiguration());
        emitDocumentedNode(child);
        emitDataNodeContainer(child);
        emitUnknownStatementNodes(child.getUnknownSchemaNodes());
        emitNotifications(child.getNotifications());
        emitActions(child.getActions());
        writer.endNode();

    }

    private void emitConstraints(final ConstraintDefinition constraints) {
        emitWhen(constraints.getWhenCondition());
        for (final MustDefinition mustCondition : constraints.getMustConstraints()) {
            emitMust(mustCondition);
        }
    }

    private void emitLeaf(final LeafSchemaNode child) {
        writer.startLeafNode(child.getQName());
        emitWhen(child.getConstraints().getWhenCondition());
        // FIXME: BUG-2444:  *(ifFeatureNode )
        emitTypeNode(child.getPath(), child.getType());
        emitUnitsNode(child.getUnits());
        emitMustNodes(child.getConstraints().getMustConstraints());
        emitDefaultNode(child.getDefault());
        emitConfigNode(child.isConfiguration());
        emitMandatoryNode(child.getConstraints().isMandatory());
        emitDocumentedNode(child);
        emitUnknownStatementNodes(child.getUnknownSchemaNodes());
        writer.endNode();

    }

    private void emitLeafList(final LeafListSchemaNode child) {
        writer.startLeafListNode(child.getQName());

        emitWhen(child.getConstraints().getWhenCondition());
        // FIXME: BUG-2444: *(ifFeatureNode )
        emitTypeNode(child.getPath(), child.getType());
        emitUnitsNode(child.getType().getUnits());
        // FIXME: BUG-2444: unitsNode /Optional
        emitMustNodes(child.getConstraints().getMustConstraints());
        emitConfigNode(child.isConfiguration());
        emitDefaultNodes(child.getDefaults());
        emitMinElementsNode(child.getConstraints().getMinElements());
        emitMaxElementsNode(child.getConstraints().getMaxElements());
        emitOrderedBy(child.isUserOrdered());
        emitDocumentedNode(child);
        emitUnknownStatementNodes(child.getUnknownSchemaNodes());
        writer.endNode();

    }

    private void emitList(final ListSchemaNode child) {
        writer.startListNode(child.getQName());
        emitWhen(child.getConstraints().getWhenCondition());

        // FIXME: BUG-2444: *(ifFeatureNode )
        emitMustNodes(child.getConstraints().getMustConstraints());
        emitKey(child.getKeyDefinition());
        emitUniqueConstraints(child.getUniqueConstraints());
        emitConfigNode(child.isConfiguration());
        emitMinElementsNode(child.getConstraints().getMinElements());
        emitMaxElementsNode(child.getConstraints().getMaxElements());
        emitOrderedBy(child.isUserOrdered());
        emitDocumentedNode(child);
        emitDataNodeContainer(child);
        emitUnknownStatementNodes(child.getUnknownSchemaNodes());
        emitNotifications(child.getNotifications());
        emitActions(child.getActions());
        writer.endNode();

    }

    private void emitMustNodes(final Set<MustDefinition> mustConstraints) {
        for (final MustDefinition must : mustConstraints) {
            emitMust(must);
        }
    }

    private void emitKey(final List<QName> keyList) {
        if (keyList != null && !keyList.isEmpty()) {
            writer.startKeyNode(keyList);
            writer.endNode();
        }
    }

    private void emitUniqueConstraints(final Collection<UniqueConstraint> uniqueConstraints) {
        for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
            emitUnique(uniqueConstraint);
        }
    }

    private void emitUnique(final UniqueConstraint uniqueConstraint) {
        writer.startUniqueNode(uniqueConstraint);
        writer.endNode();
    }

    private void emitChoice(final ChoiceSchemaNode choice) {
        writer.startChoiceNode(choice.getQName());
        emitWhen(choice.getConstraints().getWhenCondition());
        // FIXME: BUG-2444: *(ifFeatureNode )
        // FIXME: BUG-2444: defaultNode //Optional
        emitConfigNode(choice.isConfiguration());
        emitMandatoryNode(choice.getConstraints().isMandatory());
        emitDocumentedNode(choice);
        for (final ChoiceCaseNode caze : choice.getCases()) {
            // TODO: emit short case?
            emitCaseNode(caze);
        }
        emitUnknownStatementNodes(choice.getUnknownSchemaNodes());
        writer.endNode();
    }

    private void emitCaseNode(final ChoiceCaseNode caze) {
        if (!emitInstantiated && caze.isAugmenting()) {
            return;
        }
        writer.startCaseNode(caze.getQName());
        emitWhen(caze.getConstraints().getWhenCondition());
        // FIXME: BUG-2444: *(ifFeatureNode )
        emitDocumentedNode(caze);
        emitDataNodeContainer(caze);
        emitUnknownStatementNodes(caze.getUnknownSchemaNodes());
        writer.endNode();

    }

    private void emitAnyxml(final AnyXmlSchemaNode anyxml) {
        writer.startAnyxmlNode(anyxml.getQName());
        emitBodyOfDataSchemaNode(anyxml);
        writer.endNode();
    }

    private void emitAnydata(final AnyDataSchemaNode anydata) {
        writer.startAnydataNode(anydata.getQName());
        emitBodyOfDataSchemaNode(anydata);
        writer.endNode();
    }

    private void emitBodyOfDataSchemaNode(final DataSchemaNode dataSchemaNode) {
        emitWhen(dataSchemaNode.getConstraints().getWhenCondition());
        // FIXME: BUG-2444: *(ifFeatureNode )
        emitMustNodes(dataSchemaNode.getConstraints().getMustConstraints());
        emitConfigNode(dataSchemaNode.isConfiguration());
        emitMandatoryNode(dataSchemaNode.getConstraints().isMandatory());
        emitDocumentedNode(dataSchemaNode);
        emitUnknownStatementNodes(dataSchemaNode.getUnknownSchemaNodes());
    }

    private void emitUsesNode(final UsesNode usesNode) {
        if (emitUses && !usesNode.isAddedByUses() && !usesNode.isAugmenting()) {
            writer.startUsesNode(usesNode.getGroupingPath().getLastComponent());
            /*
             * FIXME: BUG-2444:
             *  whenNode /
             *  *(ifFeatureNode )
             * statusNode // Optional F
             * : descriptionNode // Optional
             * referenceNode // Optional
             */
            for (final Entry<SchemaPath, SchemaNode> refine : usesNode.getRefines().entrySet()) {
                emitRefine(refine);
            }
            for (final AugmentationSchema aug : usesNode.getAugmentations()) {
                emitUsesAugmentNode(aug);
            }
            writer.endNode();
        }
    }

    private void emitRefine(final Entry<SchemaPath, SchemaNode> refine) {
        final SchemaPath path = refine.getKey();
        final SchemaNode value = refine.getValue();
        writer.startRefineNode(path);

        if (value instanceof LeafSchemaNode) {
            emitRefineLeafNodes((LeafSchemaNode) value);
        } else if (value instanceof LeafListSchemaNode) {
            emitRefineLeafListNodes((LeafListSchemaNode) value);
        } else if (value instanceof ListSchemaNode) {
            emitRefineListNodes((ListSchemaNode) value);
        } else if (value instanceof ChoiceSchemaNode) {
            emitRefineChoiceNodes((ChoiceSchemaNode) value);
        } else if (value instanceof ChoiceCaseNode) {
            emitRefineCaseNodes((ChoiceCaseNode) value);
        } else if (value instanceof ContainerSchemaNode) {
            emitRefineContainerNodes((ContainerSchemaNode) value);
        } else if (value instanceof AnyXmlSchemaNode) {
            emitRefineAnyxmlNodes((AnyXmlSchemaNode) value);
        }
        writer.endNode();

    }

    private static <T extends SchemaNode> T getOriginalChecked(final T value) {
        final Optional<SchemaNode> original = SchemaNodeUtils.getOriginalIfPossible(value);
        Preconditions.checkArgument(original.isPresent(), "Original unmodified version of node is not present.");
        @SuppressWarnings("unchecked")
        final T ret = (T) original.get();
        return ret;
    }

    private void emitDocumentedNodeRefine(final DocumentedNode original, final DocumentedNode value) {
        if (Objects.deepEquals(original.getDescription(), value.getDescription())) {
            emitDescriptionNode(value.getDescription());
        }
        if (Objects.deepEquals(original.getReference(), value.getReference())) {
            emitReferenceNode(value.getReference());
        }
    }

    private void emitRefineContainerNodes(final ContainerSchemaNode value) {
        final ContainerSchemaNode original = getOriginalChecked(value);

        // emitMustNodes(child.getConstraints().getMustConstraints());
        if (Objects.deepEquals(original.isPresenceContainer(), value.isPresenceContainer())) {
            emitPresenceNode(value.isPresenceContainer());
        }
        if (Objects.deepEquals(original.isConfiguration(), value.isConfiguration())) {
            emitConfigNode(value.isConfiguration());
        }
        emitDocumentedNodeRefine(original, value);

    }

    private void emitRefineLeafNodes(final LeafSchemaNode value) {
        final LeafSchemaNode original = getOriginalChecked(value);

        // emitMustNodes(child.getConstraints().getMustConstraints());
        if (Objects.deepEquals(original.getDefault(), value.getDefault())) {
            emitDefaultNode(value.getDefault());
        }
        if (Objects.deepEquals(original.isConfiguration(), value.isConfiguration())) {
            emitConfigNode(value.isConfiguration());
        }
        emitDocumentedNodeRefine(original, value);
        if (Objects.deepEquals(original.getConstraints().isMandatory(), value.getConstraints().isMandatory())) {
            emitMandatoryNode(value.getConstraints().isMandatory());
        }

    }

    private void emitRefineLeafListNodes(final LeafListSchemaNode value) {
        final LeafListSchemaNode original = getOriginalChecked(value);

        // emitMustNodes(child.getConstraints().getMustConstraints());
        if (Objects.deepEquals(original.isConfiguration(), value.isConfiguration())) {
            emitConfigNode(value.isConfiguration());
        }
        if (Objects.deepEquals(original.getConstraints().getMinElements(), value.getConstraints().getMinElements())) {
            emitMinElementsNode(value.getConstraints().getMinElements());
        }
        if (Objects.deepEquals(original.getConstraints().getMaxElements(), value.getConstraints().getMaxElements())) {
            emitMaxElementsNode(value.getConstraints().getMaxElements());
        }
        emitDocumentedNodeRefine(original, value);

    }

    private void emitRefineListNodes(final ListSchemaNode value) {
        final ListSchemaNode original = getOriginalChecked(value);

        // emitMustNodes(child.getConstraints().getMustConstraints());
        if (Objects.deepEquals(original.isConfiguration(), value.isConfiguration())) {
            emitConfigNode(value.isConfiguration());
        }
        if (Objects.deepEquals(original.getConstraints().getMinElements(), value.getConstraints().getMinElements())) {
            emitMinElementsNode(value.getConstraints().getMinElements());
        }
        if (Objects.deepEquals(original.getConstraints().getMaxElements(), value.getConstraints().getMaxElements())) {
            emitMaxElementsNode(value.getConstraints().getMaxElements());
        }
        emitDocumentedNodeRefine(original, value);

    }

    private void emitRefineChoiceNodes(final ChoiceSchemaNode value) {
        final ChoiceSchemaNode original = getOriginalChecked(value);

        // FIXME: BUG-2444: defaultNode //FIXME: BUG-2444: Optional
        if (Objects.deepEquals(original.isConfiguration(), value.isConfiguration())) {
            emitConfigNode(value.isConfiguration());
        }
        if (Objects.deepEquals(original.getConstraints().isMandatory(), value.getConstraints().isMandatory())) {
            emitMandatoryNode(value.getConstraints().isMandatory());
        }
        emitDocumentedNodeRefine(original, value);

    }

    private void emitRefineCaseNodes(final ChoiceCaseNode value) {
        final ChoiceCaseNode original = getOriginalChecked(value);
        emitDocumentedNodeRefine(original, value);

    }

    private void emitRefineAnyxmlNodes(final AnyXmlSchemaNode value) {
        final AnyXmlSchemaNode original = getOriginalChecked(value);

        // FIXME: BUG-2444:  emitMustNodes(child.getConstraints().getMustConstraints());
        if (Objects.deepEquals(original.isConfiguration(), value.isConfiguration())) {
            emitConfigNode(value.isConfiguration());
        }
        if (Objects.deepEquals(original.getConstraints().isMandatory(), value.getConstraints().isMandatory())) {
            emitMandatoryNode(value.getConstraints().isMandatory());
        }
        emitDocumentedNodeRefine(original, value);

    }

    private void emitUsesAugmentNode(final AugmentationSchema aug) {
        /**
         * differs only in location in schema, otherwise currently (as of
         * RFC6020) it is same, so we could freely reuse path.
         */
        emitAugment(aug);
    }

    private void emitAugment(final AugmentationSchema augmentation) {
        writer.startAugmentNode(augmentation.getTargetPath());
        // FIXME: BUG-2444: whenNode //Optional
        // FIXME: BUG-2444: *(ifFeatureNode )

        emitStatusNode(augmentation.getStatus());
        emitDescriptionNode(augmentation.getDescription());
        emitReferenceNode(augmentation.getReference());
        for (final UsesNode uses: augmentation.getUses()) {
            emitUsesNode(uses);
        }

        for (final DataSchemaNode childNode : augmentation.getChildNodes()) {
            if (childNode instanceof ChoiceCaseNode) {
                emitCaseNode((ChoiceCaseNode) childNode);
            } else {
                emitDataSchemaNode(childNode);
            }
        }
        emitUnknownStatementNodes(augmentation.getUnknownSchemaNodes());
        emitNotifications(augmentation.getNotifications());
        emitActions(augmentation.getActions());
        writer.endNode();
    }

    private void emitUnknownStatementNodes(final List<UnknownSchemaNode> unknownNodes) {
        for (final UnknownSchemaNode unknonwnNode : unknownNodes) {
            if (!unknonwnNode.isAddedByAugmentation() && !unknonwnNode.isAddedByUses()) {
                emitUnknownStatementNode(unknonwnNode);
            }
        }
    }

    private void emitUnknownStatementNode(final UnknownSchemaNode node) {
        final StatementDefinition def = getStatementChecked(node.getNodeType());
        if (def.getArgumentName() == null) {
            writer.startUnknownNode(def);
        } else {
            writer.startUnknownNode(def, node.getNodeParameter());
        }
        emitUnknownStatementNodes(node.getUnknownSchemaNodes());
        writer.endNode();
    }

    private StatementDefinition getStatementChecked(final QName nodeType) {
        final StatementDefinition ret = extensions.get(nodeType);
        Preconditions.checkArgument(ret != null, "Unknown extension %s used during export.",nodeType);
        return ret;
    }

    private void emitWhen(final RevisionAwareXPath revisionAwareXPath) {
        if (revisionAwareXPath != null) {
            writer.startWhenNode(revisionAwareXPath);
            writer.endNode();
        }
                // FIXME: BUG-2444: descriptionNode //FIXME: BUG-2444: Optional
        // FIXME: BUG-2444: referenceNode //FIXME: BUG-2444: Optional
        // FIXME: BUG-2444: writer.endNode();)

    }

    private void emitRpc(final RpcDefinition rpc) {
        writer.startRpcNode(rpc.getQName());
        emitOperationBody(rpc);
        writer.endNode();
    }

    private void emitOperationBody(final OperationDefinition rpc) {
        // FIXME: BUG-2444: *(ifFeatureNode )
        emitStatusNode(rpc.getStatus());
        emitDescriptionNode(rpc.getDescription());
        emitReferenceNode(rpc.getReference());

        for (final TypeDefinition<?> typedef : rpc.getTypeDefinitions()) {
            emitTypedefNode(typedef);
        }
        for (final GroupingDefinition grouping : rpc.getGroupings()) {
            emitGrouping(grouping);
        }
        emitInput(rpc.getInput());
        emitOutput(rpc.getOutput());
        emitUnknownStatementNodes(rpc.getUnknownSchemaNodes());
    }

    private void emitActions(final Set<ActionDefinition> actions) {
        for (final ActionDefinition actionDefinition : actions) {
            emitAction(actionDefinition);
        }
    }

    private void emitAction(final ActionDefinition action) {
        writer.startActionNode(action.getQName());
        emitOperationBody(action);
        writer.endNode();
    }

    private void emitInput(@Nonnull final ContainerSchemaNode input) {
        if (isExplicitStatement(input)) {
            writer.startInputNode();
            emitConstraints(input.getConstraints());
            emitDataNodeContainer(input);
            emitUnknownStatementNodes(input.getUnknownSchemaNodes());
            writer.endNode();
        }

    }

    private void emitOutput(@Nonnull final ContainerSchemaNode output) {
        if (isExplicitStatement(output)) {
            writer.startOutputNode();
            emitConstraints(output.getConstraints());
            emitDataNodeContainer(output);
            emitUnknownStatementNodes(output.getUnknownSchemaNodes());
            writer.endNode();
        }

    }

    private static boolean isExplicitStatement(final ContainerSchemaNode node) {
        return node instanceof EffectiveStatement
                && ((EffectiveStatement<?, ?>) node).getDeclared().getStatementSource() == StatementSource.DECLARATION;
    }

    private void emitNotifications(final Set<NotificationDefinition> notifications) {
        for (final NotificationDefinition notification : notifications) {
            emitNotificationNode(notification);
        }
    }

    private void emitNotificationNode(final NotificationDefinition notification) {
        writer.startNotificationNode(notification.getQName());
        // FIXME: BUG-2444: *(ifFeatureNode )
        emitConstraints(notification.getConstraints());
        emitDocumentedNode(notification);
        emitDataNodeContainer(notification);
        emitUnknownStatementNodes(notification.getUnknownSchemaNodes());
        writer.endNode();

    }


    //FIXME: Probably should be moved to utils bundle.
    private static <T> boolean  isPrefix(final Iterable<T> prefix, final Iterable<T> other) {
        final Iterator<T> prefixIt = prefix.iterator();
        final Iterator<T> otherIt = other.iterator();
        while (prefixIt.hasNext()) {
            if (!otherIt.hasNext()) {
                return false;
            }
            if (!Objects.deepEquals(prefixIt.next(), otherIt.next())) {
                return false;
            }
        }
        return true;
    }

    private void emitDeviation(final Deviation deviation) {
        /*
         * FIXME: BUG-2444:  Deviation is not modeled properly and we are loosing lot of
         * information in order to export it properly
         *
         * writer.startDeviationNode(deviation.getTargetPath());
         *
         * :descriptionNode //:Optional
         *
         *
         * emitReferenceNode(deviation.getReference());
         * :(deviateNotSupportedNode :1*(deviateAddNode :deviateReplaceNode
         * :deviateDeleteNode)) :writer.endNode();
         */
    }
}
