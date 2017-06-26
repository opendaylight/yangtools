/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.primitives.UnsignedInteger;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BodyGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionContainer;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DocumentationGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.DocumentedConstraintGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LinkageGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MetaGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OperationGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
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
abstract class SchemaContextEmitter {

    final YangModuleWriter writer;
    final boolean emitInstantiated;
    final boolean emitUses;
    final Map<QName, StatementDefinition> extensions;
    final YangVersion yangVersion;

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
            final StatementTextWriter statementWriter, final boolean emitInstantiated) {
        final YangModuleWriter yangSchemaWriter = SchemaToStatementWriterAdaptor.from(statementWriter);
        final Map<QName, StatementDefinition> extensions = ExtensionStatement.mapFrom(ctx.getExtensions());
        if (module instanceof EffectiveStatement && !emitInstantiated) {
            /*
             * if module is an effective statement and we don't want to export
             * instantiated statements (e.g. statements added by uses or
             * augment) we can get declared form i.e. ModuleStatement and then
             * use DeclaredSchemaContextEmitter
             */
            new DeclaredSchemaContextEmitter(yangSchemaWriter, extensions,
                    YangVersion.parse(module.getYangVersion()).orElse(null))
                            .emitModule(((EffectiveStatement<?, ?>) module).getDeclared());
        } else {
            /*
             * if we don't have access to declared form of supplied module or we
             * want to emit also instantiated statements (e.g. statements added
             * by uses or augment), we use EffectiveSchemaContextEmitter.
             */
            new EffectiveSchemaContextEmitter(yangSchemaWriter, extensions,
                    YangVersion.parse(module.getYangVersion()).orElse(null), emitInstantiated).emitModule(module);
        }
    }

    // FIXME: Probably should be moved to utils bundle.
    static <T> boolean isPrefix(final Iterable<T> prefix, final Iterable<T> other) {
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

    static class DeclaredSchemaContextEmitter extends SchemaContextEmitter {

        DeclaredSchemaContextEmitter(final YangModuleWriter writer, final Map<QName, StatementDefinition> extensions,
                final YangVersion yangVersion) {
            super(writer, extensions, yangVersion);
        }

        void emitModule(final DeclaredStatement<?> declaredRootStmt) {
            if (declaredRootStmt instanceof ModuleStatement) {
                emitModule((ModuleStatement) declaredRootStmt);
            } else if (declaredRootStmt instanceof SubmoduleStatement) {
                emitSubmodule((SubmoduleStatement) declaredRootStmt);
            } else {
                throw new UnsupportedOperationException(
                        String.format("Yin export: unsupported declared statement %s", declaredRootStmt));
            }
        }

        private void emitModule(final ModuleStatement module) {
            super.writer.startModuleNode(module.rawArgument());
            emitModuleHeader(module);
            emitLinkageNodes(module);
            emitMetaNodes(module);
            emitRevisionNodes(module);
            emitBodyNodes(module);
            emitUnknownStatementNodes(module);
            super.writer.endNode();
        }

        private void emitModuleHeader(final ModuleStatement input) {
            emitYangVersionNode(input.getYangVersion());
            emitNamespace(input.getNamespace());
            emitPrefixNode(input.getPrefix());
        }

        private void emitSubmodule(final SubmoduleStatement submodule) {
            super.writer.startSubmoduleNode(submodule.rawArgument());
            emitSubmoduleHeaderNodes(submodule);
            emitLinkageNodes(submodule);
            emitMetaNodes(submodule);
            emitRevisionNodes(submodule);
            emitBodyNodes(submodule);
            emitUnknownStatementNodes(submodule);
            super.writer.endNode();
        }

        private void emitSubmoduleHeaderNodes(final SubmoduleStatement input) {
            emitYangVersionNode(input.getYangVersion());
            emitBelongsTo(input.getBelongsTo());
        }

        private void emitBelongsTo(final BelongsToStatement belongsTo) {
            super.writer.startBelongsToNode(belongsTo.rawArgument());
            emitPrefixNode(belongsTo.getPrefix());
            super.writer.endNode();
        }

        private void emitMetaNodes(final MetaGroup input) {
            emitOrganizationNode(input.getOrganization());
            emitContact(input.getContact());
            emitDescriptionNode(input.getDescription());
            emitReferenceNode(input.getReference());
        }

        private void emitLinkageNodes(final LinkageGroup input) {
            for (final ImportStatement importNode : input.getImports()) {
                emitImport(importNode);
            }
            for (final IncludeStatement importNode : input.getIncludes()) {
                emitInclude(importNode);
            }
        }

        private void emitRevisionNodes(final RevisionGroup input) {
            emitRevisions(input.getRevisions());
        }

        private void emitBodyNodes(final BodyGroup input) {

            for (final org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement extension : input
                    .getExtensions()) {
                emitExtension(extension);
            }
            for (final FeatureStatement definition : input.getFeatures()) {
                emitFeature(definition);
            }
            for (final IdentityStatement identity : input.getIdentities()) {
                emitIdentity(identity);
            }
            for (final DeviationStatement deviation : input.getDeviations()) {
                emitDeviation(deviation);
            }

            emitDataNodeContainer(input);

            for (final AugmentStatement augmentation : input.getAugments()) {
                emitAugment(augmentation);
            }
            for (final RpcStatement rpc : input.getRpcs()) {
                emitRpc(rpc);
            }

            emitNotifications(input.getNotifications());
        }

        private void emitDataNodeContainer(final DataDefinitionContainer input) {
            for (final DataDefinitionStatement child : input.getDataDefinitions()) {
                emitDataSchemaNode(child);
            }
        }

        private void emitDataNodeContainer(final DataDefinitionContainer.WithReusableDefinitions input) {
            for (final TypedefStatement typedef : input.getTypedefs()) {
                emitTypedefNode(typedef);
            }
            for (final GroupingStatement grouping : input.getGroupings()) {
                emitGrouping(grouping);
            }
            for (final DataDefinitionStatement child : input.getDataDefinitions()) {
                emitDataSchemaNode(child);
            }
        }

        private void emitDataSchemaNode(final DataDefinitionStatement child) {
            if (child instanceof ContainerStatement) {
                emitContainer((ContainerStatement) child);
            } else if (child instanceof LeafStatement) {
                emitLeaf((LeafStatement) child);
            } else if (child instanceof LeafListStatement) {
                emitLeafList((LeafListStatement) child);
            } else if (child instanceof ListStatement) {
                emitList((ListStatement) child);
            } else if (child instanceof ChoiceStatement) {
                emitChoice((ChoiceStatement) child);
            } else if (child instanceof AnyxmlStatement) {
                emitAnyxml((AnyxmlStatement) child);
            } else if (child instanceof AnydataStatement) {
                emitAnydata((AnydataStatement) child);
            } else if (child instanceof UsesStatement) {
                emitUsesNode((UsesStatement) child);
            } else {
                throw new UnsupportedOperationException("Not supported DataStatement type " + child.getClass());
            }
        }

        private void emitYangVersionNode(@Nullable final YangVersionStatement yangVersionStatement) {
            if (yangVersionStatement != null) {
                super.writer.startYangVersionNode(yangVersionStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitImport(final ImportStatement importNode) {
            super.writer.startImportNode(importNode.rawArgument());
            emitDocumentedNode(importNode);
            emitPrefixNode(importNode.getPrefix());
            emitRevisionDateNode(importNode.getRevisionDate());
            super.writer.endNode();
        }

        private void emitInclude(final IncludeStatement include) {
            super.writer.startIncludeNode(include.rawArgument());
            emitDocumentedNode(include);
            emitRevisionDateNode(include.getRevisionDate());
            super.writer.endNode();
        }

        private void emitNamespace(final NamespaceStatement namespaceStatement) {
            Preconditions.checkNotNull(namespaceStatement, "Namespace must not be null");
            super.writer.startNamespaceNode(namespaceStatement.getUri());
            super.writer.endNode();
        }

        private void emitPrefixNode(final PrefixStatement prefixStatement) {
            Preconditions.checkNotNull(prefixStatement, "Prefix must not be null");
            super.writer.startPrefixNode(prefixStatement.rawArgument());
            super.writer.endNode();
        }

        private void emitOrganizationNode(@Nullable final OrganizationStatement organizationStatement) {
            if (organizationStatement != null) {
                super.writer.startOrganizationNode(organizationStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitContact(@Nullable final ContactStatement contactStatement) {
            if (contactStatement != null) {
                super.writer.startContactNode(contactStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitDescriptionNode(@Nullable final DescriptionStatement descriptionStatement) {
            if (descriptionStatement != null) {
                super.writer.startDescriptionNode(descriptionStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitReferenceNode(@Nullable final ReferenceStatement referenceStatement) {
            if (referenceStatement != null) {
                super.writer.startReferenceNode(referenceStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitUnitsNode(@Nullable final UnitsStatement unitsStatement) {
            if (unitsStatement != null) {
                super.writer.startUnitsNode(unitsStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitRevisions(final Collection<? extends RevisionStatement> revisions) {
            for (final RevisionStatement revisionStatement : revisions) {
                emitRevision(revisionStatement);
            }
        }

        private void emitRevision(final RevisionStatement revision) {
            super.writer.startRevisionNode(revision.rawArgument());
            emitDocumentedNode(revision);
            super.writer.endNode();
        }

        private void emitRevisionDateNode(@Nullable final RevisionDateStatement revisionDateStatement) {
            if (revisionDateStatement != null) {
                super.writer.startRevisionDateNode(revisionDateStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitExtension(final org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement extension) {
            super.writer.startExtensionNode(extension.rawArgument());
            emitArgument(extension.getArgument());
            emitDocumentedNodeWithStatus(extension);
            emitUnknownStatementNodes(extension);
            super.writer.endNode();
        }

        private void emitArgument(@Nullable final ArgumentStatement input) {
            if (input != null) {
                super.writer.startArgumentNode(input.rawArgument());
                emitYinElement(input.getYinElement());
                super.writer.endNode();
            }
        }

        private void emitYinElement(@Nullable final YinElementStatement yinElementStatement) {
            if (yinElementStatement != null) {
                super.writer.startYinElementNode(yinElementStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitIdentity(final IdentityStatement identity) {
            super.writer.startIdentityNode(identity.rawArgument());
            emitBaseIdentities(identity.getBases());
            emitStatusNode(identity.getStatus());
            emitDescriptionNode(identity.getDescription());
            emitReferenceNode(identity.getReference());
            super.writer.endNode();
        }

        private void emitBaseIdentities(final Collection<? extends BaseStatement> collection) {
            for (final BaseStatement baseStmt : collection) {
                emitBase(baseStmt);
            }
        }

        private void emitBase(final BaseStatement baseStmt) {
            super.writer.startBaseNode(baseStmt.rawArgument());
            super.writer.endNode();
        }

        private void emitFeature(final FeatureStatement feature) {
            super.writer.startFeatureNode(feature.rawArgument());
            emitIfFeatures(feature.getIfFeatures());
            emitDocumentedNodeWithStatus(feature);
            super.writer.endNode();
        }

        private void emitIfFeatures(final Collection<? extends IfFeatureStatement> ifFeatures) {
            for (final IfFeatureStatement ifFeatureStatement : ifFeatures) {
                emitIfFeature(ifFeatureStatement);
            }
        }

        private void emitIfFeature(final IfFeatureStatement ifFeature) {
            super.writer.startIfFeatureNode(ifFeature.rawArgument());
            super.writer.endNode();
        }

        private void emitTypedefNode(final TypedefStatement typedef) {
            super.writer.startTypedefNode(typedef.rawArgument());
            emitType(typedef.getType());
            emitUnitsNode(typedef.getUnits());
            emitDefaultNode(typedef.getDefault());
            emitStatusNode(typedef.getStatus());
            emitDescriptionNode(typedef.getDescription());
            emitReferenceNode(typedef.getReference());
            emitUnknownStatementNodes(typedef);
            super.writer.endNode();
        }

        private void emitType(final TypeStatement typeStatement) {
            super.writer.startTypeNode(typeStatement.rawArgument());
            for (final DeclaredStatement<?> typeSubstmt : typeStatement.declaredSubstatements()) {
                if (typeSubstmt instanceof RangeStatement) {
                    emitRange((RangeStatement) typeSubstmt);
                } else if (typeSubstmt instanceof LengthStatement) {
                    emitLength((LengthStatement) typeSubstmt);
                } else if (typeSubstmt instanceof PatternStatement) {
                    emitPattern((PatternStatement) typeSubstmt);
                } else if (typeSubstmt instanceof FractionDigitsStatement) {
                    emitFractionDigits((FractionDigitsStatement) typeSubstmt);
                } else if (typeSubstmt instanceof EnumStatement) {
                    emitEnum((EnumStatement) typeSubstmt);
                } else if (typeSubstmt instanceof PathStatement) {
                    emitPath((PathStatement) typeSubstmt);
                } else if (typeSubstmt instanceof RequireInstanceStatement) {
                    emitRequireInstance((RequireInstanceStatement) typeSubstmt);
                } else if (typeSubstmt instanceof BaseStatement) {
                    emitBase((BaseStatement) typeSubstmt);
                } else if (typeSubstmt instanceof BitStatement) {
                    emitBit((BitStatement) typeSubstmt);
                } else if (typeSubstmt instanceof TypeStatement) {
                    emitType((TypeStatement) typeSubstmt);
                }
            }
            super.writer.endNode();
        }

        private void emitRange(final RangeStatement range) {
            super.writer.startRangeNode(range.rawArgument());
            emitDocumentedConstraint(range);
            super.writer.endNode();
        }

        private void emitFractionDigits(final FractionDigitsStatement fractionDigits) {
            super.writer.startFractionDigitsNode(fractionDigits.rawArgument());
            super.writer.endNode();
        }

        private void emitLength(final LengthStatement lengthStatement) {
            super.writer.startLengthNode(lengthStatement.rawArgument());
            emitDocumentedConstraint(lengthStatement);
            super.writer.endNode();
        }

        private void emitPattern(final PatternStatement pattern) {
            super.writer.startPatternNode(pattern.rawArgument());
            emitModifier(pattern.getModifierStatement());
            emitDocumentedConstraint(pattern);
            super.writer.endNode();
        }

        private void emitModifier(final ModifierStatement modifierStatement) {
            if (modifierStatement != null) {
                super.writer.startModifierNode(modifierStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitDefaultNodes(final Collection<? extends DefaultStatement> collection) {
            for (final DefaultStatement defaultValue : collection) {
                emitDefaultNode(defaultValue);
            }
        }

        private void emitDefaultNode(@Nullable final DefaultStatement defaultStmt) {
            if (defaultStmt != null) {
                super.writer.startDefaultNode(defaultStmt.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitEnum(final EnumStatement enumStmt) {
            super.writer.startEnumNode(enumStmt.rawArgument());
            emitDocumentedNodeWithStatus(enumStmt);
            emitValueNode(enumStmt.getValue());
            super.writer.endNode();
        }

        private void emitPath(final PathStatement path) {
            super.writer.startPathNode(path.rawArgument());
            super.writer.endNode();
        }

        private void emitRequireInstance(final RequireInstanceStatement require) {
            super.writer.startRequireInstanceNode(require.rawArgument());
            super.writer.endNode();
        }

        private void emitBit(final BitStatement bit) {
            super.writer.startBitNode(bit.rawArgument());
            emitPositionNode(bit.getPosition());
            emitDocumentedNodeWithStatus(bit);
            super.writer.endNode();
        }

        private void emitPositionNode(@Nullable final PositionStatement positionStatement) {
            if (positionStatement != null) {
                super.writer.startPositionNode(positionStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitStatusNode(@Nullable final StatusStatement statusStatement) {
            if (statusStatement != null) {
                super.writer.startStatusNode(statusStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitConfigNode(@Nullable final ConfigStatement configStatement) {
            if (configStatement != null) {
                super.writer.startConfigNode(configStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitMandatoryNode(@Nullable final MandatoryStatement mandatoryStatement) {
            if (mandatoryStatement != null) {
                super.writer.startMandatoryNode(mandatoryStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitPresenceNode(@Nullable final PresenceStatement presenceStatement) {
            if (presenceStatement != null) {
                super.writer.startPresenceNode(presenceStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitOrderedBy(@Nullable final OrderedByStatement orderedByStatement) {
            if (orderedByStatement != null) {
                super.writer.startOrderedByNode(orderedByStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitMust(@Nullable final MustStatement must) {
            if (must != null) {
                super.writer.startMustNode(must.rawArgument());
                emitErrorMessageNode(must.getErrorMessageStatement());
                emitErrorAppTagNode(must.getErrorAppTagStatement());
                emitDescriptionNode(must.getDescription());
                emitReferenceNode(must.getReference());
                super.writer.endNode();
            }
        }

        private void emitErrorMessageNode(@Nullable final ErrorMessageStatement errorMessageStatement) {
            if (errorMessageStatement != null) {
                super.writer.startErrorMessageNode(errorMessageStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitErrorAppTagNode(@Nullable final ErrorAppTagStatement errorAppTagStatement) {
            if (errorAppTagStatement != null) {
                super.writer.startErrorAppTagNode(errorAppTagStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitMinElementsNode(@Nullable final MinElementsStatement minElementsStatement) {
            if (minElementsStatement != null) {
                super.writer.startMinElementsNode(minElementsStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitMaxElementsNode(@Nullable final MaxElementsStatement maxElementsStatement) {
            if (maxElementsStatement != null) {
                super.writer.startMaxElementsNode(maxElementsStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitValueNode(@Nullable final ValueStatement valueStatement) {
            if (valueStatement != null) {
                super.writer.startValueNode(valueStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitDocumentedNodeWithStatus(final DocumentationGroup.WithStatus input) {
            emitStatusNode(input.getStatus());
            emitDocumentedNode(input);
        }

        private void emitDocumentedNode(final DocumentationGroup input) {
            emitDescriptionNode(input.getDescription());
            emitReferenceNode(input.getReference());
        }

        private void emitDocumentedConstraint(final DocumentedConstraintGroup input) {
            emitDescriptionNode(input.getDescription());
            emitReferenceNode(input.getReference());
            emitErrorMessageNode(input.getErrorMessageStatement());
            emitErrorAppTagNode(input.getErrorAppTagStatement());
        }

        private void emitGrouping(final GroupingStatement grouping) {
            super.writer.startGroupingNode(grouping.rawArgument());
            emitDocumentedNodeWithStatus(grouping);
            emitDataNodeContainer(grouping);
            emitUnknownStatementNodes(grouping);
            emitNotifications(grouping.getNotifications());
            emitActions(grouping.getActions());
            super.writer.endNode();

        }

        private void emitContainer(final ContainerStatement container) {
            super.writer.startContainerNode(container.rawArgument());
            emitWhen(container.getWhenStatement());
            emitMustNodes(container.getMusts());
            emitIfFeatures(container.getIfFeatures());
            emitPresenceNode(container.getPresence());
            emitConfigNode(container.getConfig());
            emitDocumentedNodeWithStatus(container);
            emitDataNodeContainer(container);
            emitUnknownStatementNodes(container);
            emitNotifications(container.getNotifications());
            emitActions(container.getActions());
            super.writer.endNode();

        }

        private void emitLeaf(final LeafStatement leaf) {
            super.writer.startLeafNode(leaf.rawArgument());
            emitWhen(leaf.getWhenStatement());
            emitIfFeatures(leaf.getIfFeatures());
            emitType(leaf.getType());
            emitUnitsNode(leaf.getUnits());
            emitMustNodes(leaf.getMusts());
            emitDefaultNode(leaf.getDefault());
            emitConfigNode(leaf.getConfig());
            emitMandatoryNode(leaf.getMandatory());
            emitDocumentedNodeWithStatus(leaf);
            emitUnknownStatementNodes(leaf);
            super.writer.endNode();
        }

        private void emitLeafList(final LeafListStatement leafList) {
            super.writer.startLeafListNode(leafList.rawArgument());
            emitWhen(leafList.getWhenStatement());
            emitIfFeatures(leafList.getIfFeatures());
            emitType(leafList.getType());
            emitUnitsNode(leafList.getUnits());
            emitMustNodes(leafList.getMusts());
            emitConfigNode(leafList.getConfig());
            emitDefaultNodes(leafList.getDefaults());
            emitMinElementsNode(leafList.getMinElements());
            emitMaxElementsNode(leafList.getMaxElements());
            emitOrderedBy(leafList.getOrderedBy());
            emitDocumentedNodeWithStatus(leafList);
            emitUnknownStatementNodes(leafList);
            super.writer.endNode();
        }

        private void emitList(final ListStatement list) {
            super.writer.startListNode(list.rawArgument());
            emitWhen(list.getWhenStatement());
            emitIfFeatures(list.getIfFeatures());
            emitMustNodes(list.getMusts());
            emitKey(list.getKey());
            emitUniqueConstraints(list.getUnique());
            emitConfigNode(list.getConfig());
            emitMinElementsNode(list.getMinElements());
            emitMaxElementsNode(list.getMaxElements());
            emitOrderedBy(list.getOrderedBy());
            emitDocumentedNodeWithStatus(list);
            emitDataNodeContainer(list);
            emitUnknownStatementNodes(list);
            emitNotifications(list.getNotifications());
            emitActions(list.getActions());
            super.writer.endNode();
        }

        private void emitMustNodes(final Collection<? extends MustStatement> collection) {
            for (final MustStatement must : collection) {
                emitMust(must);
            }
        }

        private void emitKey(final KeyStatement keyStatement) {
            if (keyStatement != null) {
                super.writer.startKeyNode(keyStatement.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitUniqueConstraints(final Collection<? extends UniqueStatement> collection) {
            for (final UniqueStatement uniqueConstraint : collection) {
                emitUnique(uniqueConstraint);
            }
        }

        private void emitUnique(final UniqueStatement uniqueConstraint) {
            if (uniqueConstraint != null) {
                super.writer.startUniqueNode(uniqueConstraint.rawArgument());
                super.writer.endNode();
            }
        }

        private void emitChoice(final ChoiceStatement choice) {
            super.writer.startChoiceNode(choice.rawArgument());
            emitWhen(choice.getWhenStatement());
            emitIfFeatures(choice.getIfFeatures());
            emitDefaultNode(choice.getDefault());
            emitConfigNode(choice.getConfig());
            emitMandatoryNode(choice.getMandatory());
            emitDocumentedNodeWithStatus(choice);
            emitCases(choice.getCases());
            emitUnknownStatementNodes(choice);
            super.writer.endNode();
        }

        private void emitShortCases(final Collection<? extends DeclaredStatement<?>> declaredSubstatements) {
            for (final DeclaredStatement<?> child : declaredSubstatements) {
                if (child instanceof ContainerStatement) {
                    emitContainer((ContainerStatement) child);
                } else if (child instanceof LeafStatement) {
                    emitLeaf((LeafStatement) child);
                } else if (child instanceof LeafListStatement) {
                    emitLeafList((LeafListStatement) child);
                } else if (child instanceof ListStatement) {
                    emitList((ListStatement) child);
                } else if (child instanceof ChoiceStatement) {
                    emitChoice((ChoiceStatement) child);
                } else if (child instanceof AnyxmlStatement) {
                    emitAnyxml((AnyxmlStatement) child);
                } else if (child instanceof AnydataStatement) {
                    emitAnydata((AnydataStatement) child);
                }
            }
        }

        private void emitCases(final Collection<? extends CaseStatement> cases) {
            for (final CaseStatement caze : cases) {
                if (isExplicitStatement(caze)) {
                    emitCaseNode(caze);
                } else {
                    final Collection<? extends DeclaredStatement<?>> shortCaseChilds = caze.declaredSubstatements();
                    Preconditions.checkState(shortCaseChilds.size() == 1,
                            "Only one child is allowed for each short case node");
                    emitShortCases(shortCaseChilds);
                }
            }
        }

        private void emitCaseNode(final CaseStatement caze) {
            super.writer.startCaseNode(caze.rawArgument());
            emitWhen(caze.getWhenStatement());
            emitIfFeatures(caze.getIfFeatures());
            emitDocumentedNodeWithStatus(caze);
            emitDataNodeContainer(caze);
            emitUnknownStatementNodes(caze);
            super.writer.endNode();
        }

        private void emitAnyxml(final AnyxmlStatement anyxml) {
            super.writer.startAnyxmlNode(anyxml.rawArgument());
            emitDocumentedNodeWithStatus(anyxml);
            emitWhen(anyxml.getWhenStatement());
            emitIfFeatures(anyxml.getIfFeatures());
            emitMustNodes(anyxml.getMusts());
            emitConfigNode(anyxml.getConfig());
            emitMandatoryNode(anyxml.getMandatory());
            emitDocumentedNodeWithStatus(anyxml);
            emitUnknownStatementNodes(anyxml);
            super.writer.endNode();
        }

        private void emitAnydata(final AnydataStatement anydata) {
            super.writer.startAnydataNode(anydata.rawArgument());
            emitWhen(anydata.getWhenStatement());
            emitIfFeatures(anydata.getIfFeatures());
            emitMustNodes(anydata.getMusts());
            emitConfigNode(anydata.getConfig());
            emitMandatoryNode(anydata.getMandatory());
            emitDocumentedNodeWithStatus(anydata);
            emitUnknownStatementNodes(anydata);
            super.writer.endNode();
        }

        private void emitUsesNode(final UsesStatement uses) {
            super.writer.startUsesNode(uses.rawArgument());
            emitWhen(uses.getWhenStatement());
            emitIfFeatures(uses.getIfFeatures());
            emitDocumentedNodeWithStatus(uses);
            for (final RefineStatement refine : uses.getRefines()) {
                emitRefine(refine);
            }
            for (final AugmentStatement aug : uses.getAugments()) {
                emitUsesAugmentNode(aug);
            }
            super.writer.endNode();
        }

        private void emitRefine(final RefineStatement refine) {
            super.writer.startRefineNode(refine.rawArgument());
            emitDocumentedNode(refine);
            emitIfFeatures(refine.getIfFeatures());
            emitMustNodes(refine.getMusts());
            emitPresenceNode(refine.getPresence());
            emitDefaultNodes(refine.getDefaults());
            emitConfigNode(refine.getConfig());
            emitMandatoryNode(refine.getMandatory());
            emitMinElementsNode(refine.getMinElements());
            emitMaxElementsNode(refine.getMaxElements());
            super.writer.endNode();
        }

        private void emitUsesAugmentNode(final AugmentStatement aug) {
            /**
             * differs only in location in schema, otherwise currently (as of
             * RFC6020) it is same, so we could freely reuse path.
             */
            emitAugment(aug);
        }

        private void emitAugment(final AugmentStatement augmentation) {
            super.writer.startAugmentNode(augmentation.rawArgument());
            emitIfFeatures(augmentation.getIfFeatures());
            emitWhen(augmentation.getWhenStatement());
            emitDocumentedNodeWithStatus(augmentation);
            emitDataNodeContainer(augmentation);
            emitCases(augmentation.getCases());
            emitUnknownStatementNodes(augmentation);
            emitNotifications(augmentation.getNotifications());
            emitActions(augmentation.getActions());
            super.writer.endNode();
        }

        private void emitUnknownStatementNodes(final DeclaredStatement<?> decaredStmt) {
            final Collection<? extends DeclaredStatement<?>> unknownStmts = Collections2
                    .filter(decaredStmt.declaredSubstatements(), Predicates.instanceOf(UnknownStatement.class));
            for (final DeclaredStatement<?> unknonwnStmt : unknownStmts) {
                emitUnknownStatementNode(unknonwnStmt);
            }
        }

        private void emitUnknownStatementNode(final DeclaredStatement<?> unknonwnStmt) {
            final StatementDefinition def = unknonwnStmt.statementDefinition();
            if (def.getArgumentName() == null) {
                super.writer.startUnknownNode(def);
            } else {
                super.writer.startUnknownNode(def, unknonwnStmt.rawArgument());
            }
            emitUnknownStatementNodes(unknonwnStmt);
            super.writer.endNode();
        }

        private void emitWhen(final WhenStatement whenStatement) {
            if (whenStatement != null) {
                super.writer.startWhenNode(whenStatement.rawArgument());
                emitDocumentedNode(whenStatement);
                super.writer.endNode();
            }
        }

        private void emitRpc(final RpcStatement rpc) {
            super.writer.startRpcNode(rpc.rawArgument());
            emitOperationBody(rpc);
            emitUnknownStatementNodes(rpc);
            super.writer.endNode();
        }

        private void emitOperationBody(final OperationGroup operationStmt) {
            emitIfFeatures(operationStmt.getIfFeatures());
            emitStatusNode(operationStmt.getStatus());
            emitDescriptionNode(operationStmt.getDescription());
            emitReferenceNode(operationStmt.getReference());

            for (final TypedefStatement typedef : operationStmt.getTypedefs()) {
                emitTypedefNode(typedef);
            }
            for (final GroupingStatement grouping : operationStmt.getGroupings()) {
                emitGrouping(grouping);
            }
            emitInput(operationStmt.getInput());
            emitOutput(operationStmt.getOutput());
        }

        private void emitActions(final Collection<? extends ActionStatement> collection) {
            for (final ActionStatement actionDefinition : collection) {
                emitAction(actionDefinition);
            }
        }

        private void emitAction(final ActionStatement actionDefinition) {
            super.writer.startActionNode(actionDefinition.rawArgument());
            emitOperationBody(actionDefinition);
            emitUnknownStatementNodes(actionDefinition);
            super.writer.endNode();
        }

        private void emitInput(final InputStatement inputStatement) {
            if (isExplicitStatement(inputStatement)) {
                super.writer.startInputNode();
                emitMustNodes(inputStatement.getMusts());
                emitDataNodeContainer(inputStatement);
                emitUnknownStatementNodes(inputStatement);
                super.writer.endNode();
            }
        }

        private void emitOutput(final OutputStatement output) {
            if (isExplicitStatement(output)) {
                super.writer.startOutputNode();
                emitMustNodes(output.getMusts());
                emitDataNodeContainer(output);
                emitUnknownStatementNodes(output);
                super.writer.endNode();
            }
        }

        private static boolean isExplicitStatement(final DeclaredStatement<?> stmt) {
            return stmt != null && stmt.getStatementSource() == StatementSource.DECLARATION;
        }

        private void emitNotifications(final Collection<? extends NotificationStatement> collection) {
            for (final NotificationStatement notification : collection) {
                emitNotificationNode(notification);
            }
        }

        private void emitNotificationNode(final NotificationStatement notification) {
            super.writer.startNotificationNode(notification.rawArgument());
            emitIfFeatures(notification.getIfFeatures());
            emitMustNodes(notification.getMusts());
            emitDocumentedNodeWithStatus(notification);
            emitDataNodeContainer(notification);
            emitUnknownStatementNodes(notification);
            super.writer.endNode();
        }

        private void emitDeviation(final DeviationStatement deviation) {
            super.writer.startDeviationNode(deviation.rawArgument());
            emitDeviateStatements(deviation.getDeviateStatements());
            emitUnknownStatementNodes(deviation);
            super.writer.endNode();
        }

        private void emitDeviateStatements(final Collection<? extends DeviateStatement> deviateStatements) {
            for (final DeviateStatement deviateStatement : deviateStatements) {
                emitDeviate(deviateStatement);
            }
        }

        private void emitDeviate(final DeviateStatement deviateStatement) {
            super.writer.startDeviateNode(deviateStatement.rawArgument());
            /*
             * :FIXME Currently, DeviateStatementImpl contains implementation
             * for all deviate types (i.e. add, replace, delete). However it
             * would be better to create subinterfaces of DeviateStatement for
             * each deviate type (i.e. AddDeviateStatement,
             * ReplaceDeviateStatement,..) and create argument specific supports
             * (i.e. definitions) for each deviate type (very similarly like by
             * TypeStatement).
             */
            for (final DeclaredStatement<?> child : deviateStatement.declaredSubstatements()) {
                if (child instanceof MustStatement) {
                    emitMust((MustStatement) child);
                } else if (child instanceof DefaultStatement) {
                    emitDefaultNode((DefaultStatement) child);
                } else if (child instanceof UniqueStatement) {
                    emitUnique((UniqueStatement) child);
                } else if (child instanceof UnitsStatement) {
                    emitUnitsNode((UnitsStatement) child);
                } else if (child instanceof TypeStatement) {
                    emitType((TypeStatement) child);
                } else if (child instanceof MinElementsStatement) {
                    emitMinElementsNode((MinElementsStatement) child);
                } else if (child instanceof MaxElementsStatement) {
                    emitMaxElementsNode((MaxElementsStatement) child);
                } else if (child instanceof MandatoryStatement) {
                    emitMandatoryNode((MandatoryStatement) child);
                } else if (child instanceof ConfigStatement) {
                    emitConfigNode((ConfigStatement) child);
                } else if (child instanceof UnknownStatement) {
                    emitUnknownStatementNode((UnknownStatement<?>) child);
                }
            }
            super.writer.endNode();
        }
    }

    static class EffectiveSchemaContextEmitter extends SchemaContextEmitter {

        EffectiveSchemaContextEmitter(final YangModuleWriter writer, final Map<QName, StatementDefinition> extensions,
                final YangVersion yangVersion, final boolean emitInstantiated) {
            super(writer, extensions, yangVersion, emitInstantiated, true);
        }

        void emitModule(final Module input) {
            super.writer.startModuleNode(input.getName());
            emitModuleHeader(input);
            emitLinkageNodes(input);
            emitMetaNodes(input);
            emitRevisionNodes(input);
            emitBodyNodes(input);
            super.writer.endNode();
        }

        private void emitModuleHeader(final Module input) {
            emitYangVersionNode(input.getYangVersion());
            emitNamespace(input.getNamespace());
            emitPrefixNode(input.getPrefix());
        }

        @SuppressWarnings("unused")
        private void emitSubmodule(final String input) {
            /*
             * FIXME: BUG-2444: Implement submodule export
             *
             * submoduleHeaderNodes linkageNodes metaNodes revisionNodes
             * bodyNodes super.writer.endNode();
             */
        }

        @SuppressWarnings("unused")
        private void emitSubmoduleHeaderNodes(final Module input) {
            /*
             * FIXME: BUG-2444: Implement submodule headers properly
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
             * FIXME: BUG-2444: Emit include statements
             */
        }

        private void emitRevisionNodes(final Module input) {
            /*
             * FIXME: BUG-2444: emit revisions properly, when parsed model will
             * provide enough information
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
            if (!super.emitInstantiated && (child.isAddedByUses() || child.isAugmenting())) {
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
            super.writer.startYangVersionNode(input);
            super.writer.endNode();
        }

        private void emitImport(final ModuleImport importNode) {
            super.writer.startImportNode(importNode.getModuleName());
            emitDescriptionNode(importNode.getDescription());
            emitReferenceNode(importNode.getReference());
            emitPrefixNode(importNode.getPrefix());
            emitRevisionDateNode(importNode.getRevision());
            super.writer.endNode();
        }

        @SuppressWarnings("unused")
        private void emitInclude(final String input) {
            /*
             * FIXME: BUG-2444: Implement proper export of include statements
             * startIncludeNode(IdentifierHelper.getIdentifier(String :input));
             *
             *
             * :revisionDateNode :super.writer.endNode();)
             */
        }

        private void emitNamespace(final URI uri) {
            super.writer.startNamespaceNode(uri);
            super.writer.endNode();

        }

        private void emitPrefixNode(final String input) {
            super.writer.startPrefixNode(input);
            super.writer.endNode();

        }

        @SuppressWarnings("unused")
        private void emitBelongsTo(final String input) {
            /*
             * FIXME: BUG-2444: Implement proper export of belongs-to statements
             * startIncludeNode(IdentifierHelper.getIdentifier(String :input));
             *
             *
             * :super.writer.startBelongsToNode(IdentifierHelper.getIdentifier(
             * String :input));
             *
             *
             * :prefixNode :super.writer.endNode();
             *
             */

        }

        private void emitOrganizationNode(final String input) {
            if (!Strings.isNullOrEmpty(input)) {
                super.writer.startOrganizationNode(input);
                super.writer.endNode();
            }
        }

        private void emitContact(final String input) {
            if (!Strings.isNullOrEmpty(input)) {
                super.writer.startContactNode(input);
                super.writer.endNode();
            }
        }

        private void emitDescriptionNode(@Nullable final String input) {
            if (!Strings.isNullOrEmpty(input)) {
                super.writer.startDescriptionNode(input);
                super.writer.endNode();
            }
        }

        private void emitReferenceNode(@Nullable final String input) {
            if (!Strings.isNullOrEmpty(input)) {
                super.writer.startReferenceNode(input);
                super.writer.endNode();
            }
        }

        private void emitUnitsNode(@Nullable final String input) {
            if (!Strings.isNullOrEmpty(input)) {
                super.writer.startUnitsNode(input);
                super.writer.endNode();
            }
        }

        private void emitRevision(final Date date) {
            super.writer.startRevisionNode(date);

            //
            // FIXME: BUG-2444: FIXME: BUG-2444: BUG-2417: descriptionNode
            // //FIXME: BUG-2444: Optional
            // FIXME: BUG-2444: FIXME: BUG-2444: BUG-2417: referenceNode
            // //FIXME: BUG-2444: Optional
            super.writer.endNode();

        }

        private void emitRevisionDateNode(@Nullable final Date date) {
            if (date != null) {
                super.writer.startRevisionDateNode(date);
                super.writer.endNode();
            }
        }

        private void emitExtension(final ExtensionDefinition extension) {
            super.writer.startExtensionNode(extension.getQName());
            emitArgument(extension.getArgument(), extension.isYinElement());
            emitStatusNode(extension.getStatus());
            emitDescriptionNode(extension.getDescription());
            emitReferenceNode(extension.getReference());
            emitUnknownStatementNodes(extension.getUnknownSchemaNodes());
            super.writer.endNode();

        }

        private void emitArgument(final @Nullable String input, final boolean yinElement) {
            if (input != null) {
                super.writer.startArgumentNode(input);
                emitYinElement(yinElement);
                super.writer.endNode();
            }

        }

        private void emitYinElement(final boolean yinElement) {
            super.writer.startYinElementNode(yinElement);
            super.writer.endNode();

        }

        private void emitIdentity(final IdentitySchemaNode identity) {
            super.writer.startIdentityNode(identity.getQName());
            emitBaseIdentities(identity.getBaseIdentities());
            emitStatusNode(identity.getStatus());
            emitDescriptionNode(identity.getDescription());
            emitReferenceNode(identity.getReference());
            super.writer.endNode();
        }

        private void emitBaseIdentities(final Set<IdentitySchemaNode> identities) {
            for (final IdentitySchemaNode identitySchemaNode : identities) {
                emitBase(identitySchemaNode.getQName());
            }
        }

        private void emitBase(final QName qName) {
            super.writer.startBaseNode(qName);
            super.writer.endNode();
        }

        private void emitFeature(final FeatureDefinition definition) {
            super.writer.startFeatureNode(definition.getQName());

            // FIXME: BUG-2444: FIXME: BUG-2444: Expose ifFeature
            // *(ifFeatureNode )
            emitStatusNode(definition.getStatus());
            emitDescriptionNode(definition.getDescription());
            emitReferenceNode(definition.getReference());
            super.writer.endNode();

        }

        @SuppressWarnings("unused")
        private void emitIfFeature(final String input) {
            /*
             * FIXME: BUG-2444: Implement proper export of include statements
             * startIncludeNode(IdentifierHelper.getIdentifier(String :input));
             *
             */
        }

        private void emitTypedefNode(final TypeDefinition<?> typedef) {
            super.writer.startTypedefNode(typedef.getQName());
            // Differentiate between derived type and existing type
            // name.
            emitTypeNodeDerived(typedef);
            emitUnitsNode(typedef.getUnits());
            emitDefaultNode(typedef.getDefaultValue());
            emitStatusNode(typedef.getStatus());
            emitDescriptionNode(typedef.getDescription());
            emitReferenceNode(typedef.getReference());
            emitUnknownStatementNodes(typedef.getUnknownSchemaNodes());
            super.writer.endNode();

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
            super.writer.startTypeNode(typeDefinition.getQName());
            super.writer.endNode();

        }

        private void emitTypeNodeDerived(final TypeDefinition<?> typeDefinition) {
            final TypeDefinition<?> b = typeDefinition.getBaseType();
            final TypeDefinition<?> baseType = b == null ? typeDefinition : b;
            super.writer.startTypeNode(baseType.getQName());
            emitTypeBodyNodes(typeDefinition);
            super.writer.endNode();

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
            // FIXME: BUG-2444: Wrong decomposition in API, should be
            // LenghtConstraint
            // which contains ranges.
            if (!list.isEmpty()) {
                super.writer.startRangeNode(toRangeString(list));
                final RangeConstraint first = list.iterator().next();
                emitErrorMessageNode(first.getErrorMessage());
                emitErrorAppTagNode(first.getErrorAppTag());
                emitDescriptionNode(first.getDescription());
                emitReferenceNode(first.getReference());
                super.writer.endNode();
            }

        }

        private void emitDecimal64Specification(final DecimalTypeDefinition typeDefinition) {
            emitFranctionDigitsNode(typeDefinition.getFractionDigits());
            emitRangeNodeOptional(typeDefinition.getRangeConstraints());

        }

        private void emitFranctionDigitsNode(final Integer fractionDigits) {
            super.writer.startFractionDigitsNode(fractionDigits);
            super.writer.endNode();
        }

        private void emitStringRestrictions(final StringTypeDefinition typeDef) {

            // FIXME: BUG-2444: Wrong decomposition in API, should be
            // LenghtConstraint
            // which contains ranges.
            emitLength(typeDef.getLengthConstraints());

            for (final PatternConstraint pattern : typeDef.getPatternConstraints()) {
                emitPatternNode(pattern);
            }

        }

        private void emitLength(final List<LengthConstraint> list) {
            if (!list.isEmpty()) {
                super.writer.startLengthNode(toLengthString(list));
                // FIXME: BUG-2444: Workaround for incorrect decomposition in
                // API
                final LengthConstraint first = list.iterator().next();
                emitErrorMessageNode(first.getErrorMessage());
                emitErrorAppTagNode(first.getErrorAppTag());
                emitDescriptionNode(first.getDescription());
                emitReferenceNode(first.getReference());
                super.writer.endNode();
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
            super.writer.startPatternNode(pattern.getRawRegularExpression());
            // FIXME: BUG-2444: Optional
            emitErrorMessageNode(pattern.getErrorMessage());
            // FIXME: BUG-2444: Optional
            emitErrorAppTagNode(pattern.getErrorAppTag());
            emitDescriptionNode(pattern.getDescription());
            emitModifier(pattern.getModifier());
            super.writer.endNode();
        }

        private void emitModifier(final ModifierKind modifier) {
            if (modifier != null) {
                super.writer.startModifierNode(modifier);
                super.writer.endNode();
            }
        }

        private void emitDefaultNodes(final Collection<String> defaults) {
            for (final String defaultValue : defaults) {
                emitDefaultNode(defaultValue);
            }
        }

        private void emitDefaultNode(@Nullable final Object object) {
            if (object != null) {
                super.writer.startDefaultNode(object.toString());
                super.writer.endNode();
            }
        }

        private void emitEnumSpecification(final EnumTypeDefinition typeDefinition) {
            for (final EnumPair enumValue : typeDefinition.getValues()) {
                emitEnumNode(enumValue);
            }
        }

        private void emitEnumNode(final EnumPair enumValue) {
            super.writer.startEnumNode(enumValue.getName());
            emitValueNode(enumValue.getValue());
            emitStatusNode(enumValue.getStatus());
            emitDescriptionNode(enumValue.getDescription());
            emitReferenceNode(enumValue.getReference());
            super.writer.endNode();
        }

        private void emitLeafrefSpecification(final LeafrefTypeDefinition typeDefinition) {
            emitPathNode(typeDefinition.getPathStatement());
            if (YangVersion.VERSION_1_1 == super.yangVersion) {
                emitRequireInstanceNode(typeDefinition.requireInstance());
            }
        }

        private void emitPathNode(final RevisionAwareXPath revisionAwareXPath) {
            super.writer.startPathNode(revisionAwareXPath);
            super.writer.endNode();
        }

        private void emitRequireInstanceNode(final boolean require) {
            super.writer.startRequireInstanceNode(require);
            super.writer.endNode();
        }

        private void emitInstanceIdentifierSpecification(final InstanceIdentifierTypeDefinition typeDefinition) {
            emitRequireInstanceNode(typeDefinition.requireInstance());
        }

        private void emitIdentityrefSpecification(final IdentityrefTypeDefinition typeDefinition) {
            emitBaseIdentities(typeDefinition.getIdentities());
        }

        private void emitUnionSpecification(final UnionTypeDefinition typeDefinition) {
            for (final TypeDefinition<?> subtype : typeDefinition.getTypes()) {
                // FIXME: BUG-2444: What if we have locally modified types here?
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
            super.writer.startBitNode(bit.getName());
            emitPositionNode(bit.getPosition());
            emitStatusNode(bit.getStatus());
            emitDescriptionNode(bit.getDescription());
            emitReferenceNode(bit.getReference());
            super.writer.endNode();
        }

        private void emitPositionNode(@Nullable final Long position) {
            if (position != null) {
                super.writer.startPositionNode(UnsignedInteger.valueOf(position));
                super.writer.endNode();
            }
        }

        private void emitStatusNode(@Nullable final Status status) {
            if (status != null) {
                super.writer.startStatusNode(status);
                super.writer.endNode();
            }
        }

        private void emitConfigNode(final boolean config) {
            super.writer.startConfigNode(config);
            super.writer.endNode();
        }

        private void emitMandatoryNode(final boolean mandatory) {
            super.writer.startMandatoryNode(mandatory);
            super.writer.endNode();
        }

        private void emitPresenceNode(final boolean presence) {
            super.writer.startPresenceNode(presence);
            super.writer.endNode();
        }

        private void emitOrderedBy(final boolean userOrdered) {
            if (userOrdered) {
                super.writer.startOrderedByNode("user");
            } else {
                super.writer.startOrderedByNode("system");
            }
            super.writer.endNode();
        }

        private void emitMust(@Nullable final MustDefinition mustCondition) {
            if (mustCondition != null && mustCondition.getXpath() != null) {
                super.writer.startMustNode(mustCondition.getXpath());
                emitErrorMessageNode(mustCondition.getErrorMessage());
                emitErrorAppTagNode(mustCondition.getErrorAppTag());
                emitDescriptionNode(mustCondition.getDescription());
                emitReferenceNode(mustCondition.getReference());
                super.writer.endNode();
            }

        }

        private void emitErrorMessageNode(@Nullable final String input) {
            if (input != null && !input.isEmpty()) {
                super.writer.startErrorMessageNode(input);
                super.writer.endNode();
            }
        }

        private void emitErrorAppTagNode(final String input) {
            if (input != null && !input.isEmpty()) {
                super.writer.startErrorAppTagNode(input);
                super.writer.endNode();
            }
        }

        private void emitMinElementsNode(final Integer min) {
            if (min != null) {
                super.writer.startMinElementsNode(min);
                super.writer.endNode();
            }
        }

        private void emitMaxElementsNode(final Integer max) {
            if (max != null) {
                super.writer.startMaxElementsNode(max);
                super.writer.endNode();
            }
        }

        private void emitValueNode(@Nullable final Integer value) {
            if (value != null) {
                super.writer.startValueNode(value);
                super.writer.endNode();
            }
        }

        private void emitDocumentedNode(final DocumentedNode.WithStatus input) {
            emitStatusNode(input.getStatus());
            emitDescriptionNode(input.getDescription());
            emitReferenceNode(input.getReference());
        }

        private void emitGrouping(final GroupingDefinition grouping) {
            super.writer.startGroupingNode(grouping.getQName());
            emitDocumentedNode(grouping);
            emitDataNodeContainer(grouping);
            emitUnknownStatementNodes(grouping.getUnknownSchemaNodes());
            emitNotifications(grouping.getNotifications());
            emitActions(grouping.getActions());
            super.writer.endNode();

        }

        private void emitContainer(final ContainerSchemaNode child) {
            super.writer.startContainerNode(child.getQName());

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
            super.writer.endNode();

        }

        private void emitConstraints(final ConstraintDefinition constraints) {
            emitWhen(constraints.getWhenCondition());
            for (final MustDefinition mustCondition : constraints.getMustConstraints()) {
                emitMust(mustCondition);
            }
        }

        private void emitLeaf(final LeafSchemaNode child) {
            super.writer.startLeafNode(child.getQName());
            emitWhen(child.getConstraints().getWhenCondition());
            // FIXME: BUG-2444: *(ifFeatureNode )
            emitTypeNode(child.getPath(), child.getType());
            emitUnitsNode(child.getUnits());
            emitMustNodes(child.getConstraints().getMustConstraints());
            emitDefaultNode(child.getDefault());
            emitConfigNode(child.isConfiguration());
            emitMandatoryNode(child.getConstraints().isMandatory());
            emitDocumentedNode(child);
            emitUnknownStatementNodes(child.getUnknownSchemaNodes());
            super.writer.endNode();

        }

        private void emitLeafList(final LeafListSchemaNode child) {
            super.writer.startLeafListNode(child.getQName());

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
            super.writer.endNode();

        }

        private void emitList(final ListSchemaNode child) {
            super.writer.startListNode(child.getQName());
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
            super.writer.endNode();

        }

        private void emitMustNodes(final Set<MustDefinition> mustConstraints) {
            for (final MustDefinition must : mustConstraints) {
                emitMust(must);
            }
        }

        private void emitKey(final List<QName> keyList) {
            if (keyList != null && !keyList.isEmpty()) {
                super.writer.startKeyNode(keyList);
                super.writer.endNode();
            }
        }

        private void emitUniqueConstraints(final Collection<UniqueConstraint> uniqueConstraints) {
            for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
                emitUnique(uniqueConstraint);
            }
        }

        private void emitUnique(final UniqueConstraint uniqueConstraint) {
            super.writer.startUniqueNode(uniqueConstraint);
            super.writer.endNode();
        }

        private void emitChoice(final ChoiceSchemaNode choice) {
            super.writer.startChoiceNode(choice.getQName());
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
            super.writer.endNode();
        }

        private void emitCaseNode(final ChoiceCaseNode caze) {
            if (!super.emitInstantiated && caze.isAugmenting()) {
                return;
            }
            super.writer.startCaseNode(caze.getQName());
            emitWhen(caze.getConstraints().getWhenCondition());
            // FIXME: BUG-2444: *(ifFeatureNode )
            emitDocumentedNode(caze);
            emitDataNodeContainer(caze);
            emitUnknownStatementNodes(caze.getUnknownSchemaNodes());
            super.writer.endNode();

        }

        private void emitAnyxml(final AnyXmlSchemaNode anyxml) {
            super.writer.startAnyxmlNode(anyxml.getQName());
            emitBodyOfDataSchemaNode(anyxml);
            super.writer.endNode();
        }

        private void emitAnydata(final AnyDataSchemaNode anydata) {
            super.writer.startAnydataNode(anydata.getQName());
            emitBodyOfDataSchemaNode(anydata);
            super.writer.endNode();
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
            if (super.emitUses && !usesNode.isAddedByUses() && !usesNode.isAugmenting()) {
                super.writer.startUsesNode(usesNode.getGroupingPath().getLastComponent());
                /*
                 * FIXME: BUG-2444: whenNode / *(ifFeatureNode ) statusNode //
                 * Optional F : descriptionNode // Optional referenceNode //
                 * Optional
                 */
                for (final Entry<SchemaPath, SchemaNode> refine : usesNode.getRefines().entrySet()) {
                    emitRefine(refine);
                }
                for (final AugmentationSchema aug : usesNode.getAugmentations()) {
                    emitUsesAugmentNode(aug);
                }
                super.writer.endNode();
            }
        }

        private void emitRefine(final Entry<SchemaPath, SchemaNode> refine) {
            final SchemaPath path = refine.getKey();
            final SchemaNode value = refine.getValue();
            super.writer.startRefineNode(path);

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
            super.writer.endNode();

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
            if (Objects.deepEquals(original.getConstraints().getMinElements(),
                    value.getConstraints().getMinElements())) {
                emitMinElementsNode(value.getConstraints().getMinElements());
            }
            if (Objects.deepEquals(original.getConstraints().getMaxElements(),
                    value.getConstraints().getMaxElements())) {
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
            if (Objects.deepEquals(original.getConstraints().getMinElements(),
                    value.getConstraints().getMinElements())) {
                emitMinElementsNode(value.getConstraints().getMinElements());
            }
            if (Objects.deepEquals(original.getConstraints().getMaxElements(),
                    value.getConstraints().getMaxElements())) {
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

            // FIXME: BUG-2444:
            // emitMustNodes(child.getConstraints().getMustConstraints());
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
            super.writer.startAugmentNode(augmentation.getTargetPath());
            // FIXME: BUG-2444: whenNode //Optional
            // FIXME: BUG-2444: *(ifFeatureNode )

            emitStatusNode(augmentation.getStatus());
            emitDescriptionNode(augmentation.getDescription());
            emitReferenceNode(augmentation.getReference());
            for (final UsesNode uses : augmentation.getUses()) {
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
            super.writer.endNode();
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
                super.writer.startUnknownNode(def);
            } else {
                super.writer.startUnknownNode(def, node.getNodeParameter());
            }
            emitUnknownStatementNodes(node.getUnknownSchemaNodes());
            super.writer.endNode();
        }

        private StatementDefinition getStatementChecked(final QName nodeType) {
            final StatementDefinition ret = super.extensions.get(nodeType);
            Preconditions.checkArgument(ret != null, "Unknown extension %s used during export.", nodeType);
            return ret;
        }

        private void emitWhen(final RevisionAwareXPath revisionAwareXPath) {
            if (revisionAwareXPath != null) {
                super.writer.startWhenNode(revisionAwareXPath);
                super.writer.endNode();
            }
            // FIXME: BUG-2444: descriptionNode //FIXME: BUG-2444: Optional
            // FIXME: BUG-2444: referenceNode //FIXME: BUG-2444: Optional
            // FIXME: BUG-2444: super.writer.endNode();)

        }

        private void emitRpc(final RpcDefinition rpc) {
            super.writer.startRpcNode(rpc.getQName());
            emitOperationBody(rpc);
            super.writer.endNode();
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
            // :FIXME add addedByUses & addedByAugmentation in API and perform
            // check here..
            super.writer.startActionNode(action.getQName());
            emitOperationBody(action);
            super.writer.endNode();
        }

        private void emitInput(@Nonnull final ContainerSchemaNode input) {
            if (isExplicitStatement(input)) {
                super.writer.startInputNode();
                emitConstraints(input.getConstraints());
                emitDataNodeContainer(input);
                emitUnknownStatementNodes(input.getUnknownSchemaNodes());
                super.writer.endNode();
            }

        }

        private void emitOutput(@Nonnull final ContainerSchemaNode output) {
            if (isExplicitStatement(output)) {
                super.writer.startOutputNode();
                emitConstraints(output.getConstraints());
                emitDataNodeContainer(output);
                emitUnknownStatementNodes(output.getUnknownSchemaNodes());
                super.writer.endNode();
            }
        }

        private static boolean isExplicitStatement(final ContainerSchemaNode node) {
            return node instanceof EffectiveStatement && ((EffectiveStatement<?, ?>) node).getDeclared()
                    .getStatementSource() == StatementSource.DECLARATION;
        }

        private void emitNotifications(final Set<NotificationDefinition> notifications) {
            for (final NotificationDefinition notification : notifications) {
                emitNotificationNode(notification);
            }
        }

        private void emitNotificationNode(final NotificationDefinition notification) {
            // :FIXME add addedByUses & addedByAugmentation in API and perform
            // check here..

            super.writer.startNotificationNode(notification.getQName());
            // FIXME: BUG-2444: *(ifFeatureNode )
            emitConstraints(notification.getConstraints());
            emitDocumentedNode(notification);
            emitDataNodeContainer(notification);
            emitUnknownStatementNodes(notification.getUnknownSchemaNodes());
            super.writer.endNode();

        }

        private void emitDeviation(final Deviation deviation) {
            /*
             * FIXME: BUG-2444: Deviation is not modeled properly and we are
             * loosing lot of information in order to export it properly
             *
             * super.writer.startDeviationNode(deviation.getTargetPath());
             *
             * :descriptionNode //:Optional
             *
             *
             * emitReferenceNode(deviation.getReference());
             * :(deviateNotSupportedNode :1*(deviateAddNode :deviateReplaceNode
             * :deviateDeleteNode)) :super.writer.endNode();
             */
        }
    }
}
