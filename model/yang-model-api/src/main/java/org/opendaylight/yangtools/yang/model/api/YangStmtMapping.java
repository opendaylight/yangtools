/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;

/**
 * Mapping for both RFC6020 and RFC7950 statements.
 */
// FIXME: eliminate this class
@Beta
public enum YangStmtMapping implements StatementDefinition {
    ANYDATA(AnydataStatement.class, AnydataEffectiveStatement.class, "anydata", "name"),
    ANYXML(AnyxmlStatement.class, AnyxmlEffectiveStatement.class, "anyxml", "name"),
    AUGMENT(AugmentStatement.class, AugmentEffectiveStatement.class, "augment", "target-node"),
    BASE(BaseStatement.class, BaseEffectiveStatement.class, "base", "name"),
    BELONGS_TO(BelongsToStatement.class, BelongsToEffectiveStatement.class, "belongs-to", "module"),
    CASE(CaseStatement.class, CaseEffectiveStatement.class, "case", "name"),
    CHOICE(ChoiceStatement.class, ChoiceEffectiveStatement.class, "choice", "name"),
    CONTAINER(ContainerStatement.class, ContainerEffectiveStatement.class, "container", "name"),
    DEFAULT(DefaultStatement.class, DefaultEffectiveStatement.class, "default", "value"),
    DEVIATE(DeviateStatement.class, DeviateEffectiveStatement.class, "deviate", "value"),
    DEVIATION(DeviationStatement.class, DeviationEffectiveStatement.class, "deviation", "target-node"),
    EXTENSION(ExtensionStatement.class, ExtensionEffectiveStatement.class, "extension", "name"),
    GROUPING(GroupingStatement.class, GroupingEffectiveStatement.class, "grouping", "name"),
    IDENTITY(IdentityStatement.class, IdentityEffectiveStatement.class, "identity", "name"),
    IMPORT(ImportStatement.class, ImportEffectiveStatement.class, "import", "module"),
    INCLUDE(IncludeStatement.class, IncludeEffectiveStatement.class, "include", "module"),
    LEAF(LeafStatement.class, LeafEffectiveStatement.class, "leaf", "name"),
    LEAF_LIST(LeafListStatement.class, LeafListEffectiveStatement.class, "leaf-list", "name"),
    LIST(ListStatement.class, ListEffectiveStatement.class, "list", "name"),
    MODIFIER(ModifierStatement.class, ModifierEffectiveStatement.class, "modifier", "value"),
    MODULE(ModuleStatement.class, ModuleEffectiveStatement.class, "module", "name"),
    MUST(MustStatement.class, MustEffectiveStatement.class, "must", "condition"),
    NAMESPACE(NamespaceStatement.class, NamespaceEffectiveStatement.class, "namespace", "uri"),
    NOTIFICATION(NotificationStatement.class, NotificationEffectiveStatement.class, "notification", "name"),
    PATH(PathStatement.class, PathEffectiveStatement.class, "path", "value"),
    PREFIX(PrefixStatement.class, PrefixEffectiveStatement.class, "prefix", "value"),
    REFINE(RefineStatement.class, RefineEffectiveStatement.class, "refine", "target-node"),
    REVISION(RevisionStatement.class, RevisionEffectiveStatement.class, "revision", "date"),
    REVISION_DATE(RevisionDateStatement.class, RevisionDateEffectiveStatement.class, "revision-date", "date"),
    SUBMODULE(SubmoduleStatement.class, SubmoduleEffectiveStatement.class, "submodule", "name"),
    @SuppressWarnings({ "unchecked", "rawtypes" })
    TYPE(TypeStatement.class, (Class) TypeEffectiveStatement.class, "type", "name"),
    TYPEDEF(TypedefStatement.class, TypedefEffectiveStatement.class, "typedef", "name"),
    USES(UsesStatement.class, UsesEffectiveStatement.class, "uses", "name"),
    WHEN(WhenStatement.class, WhenEffectiveStatement.class, "when", "condition");

    private final @NonNull Class<? extends DeclaredStatement<?>> declaredRepresentation;
    private final @NonNull Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation;
    private final @NonNull QName statementName;
    private final @Nullable ArgumentDefinition argumentDefinition;

    YangStmtMapping(final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String name, final String argName) {
        this(declared, effective, name, argName, false);
    }

    YangStmtMapping(final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String name, final String argName,
            final boolean yinElement) {
        declaredRepresentation = requireNonNull(declared);
        effectiveRepresentation = requireNonNull(effective);
        statementName = qualifyName(name);
        argumentDefinition = new ArgumentDefinition(qualifyName(argName), yinElement);
    }

    private static @NonNull QName qualifyName(final String name) {
        return QName.create(YangConstants.RFC6020_YIN_MODULE, name).intern();
    }

    @Override
    public QName statementName() {
        return statementName;
    }

    @Override
    public ArgumentDefinition argumentDefinition() {
        return argumentDefinition;
    }

    @Override
    public Class<? extends DeclaredStatement<?>> declaredRepresentation() {
        return declaredRepresentation;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation() {
        return effectiveRepresentation;
    }

    @Override
    public String toString() {
        return StatementDefinition.toString(this);
    }
}
