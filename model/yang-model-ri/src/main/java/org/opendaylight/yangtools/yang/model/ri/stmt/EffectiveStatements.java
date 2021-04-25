/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyRequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyStatusStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyYangVersionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.AbstractLeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.ActionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.AugmentEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.ChoiceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.ContainerEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.DeclaredCaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.DeclaredInputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.DeclaredOutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.DeviateEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.DeviationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyAnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyAnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyBaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyBelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyBitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyDefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyDescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyEnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyForeignKeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyFractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyIdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyIfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyIncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyLeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyLengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyLocalKeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyMandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyMaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyMinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyMustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyNamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyOrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyOrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyPathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyPatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyPositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyPrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyPresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyRangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyRequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyRevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyRevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyStatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyUniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyUnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyWhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyYangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyYinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.ExtensionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.GroupingEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.ImportEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.NotificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularAnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularAnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularBaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularBelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularBitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularDefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularDescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularEnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularForeignKeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularFractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularIdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularIfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularIncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularLeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularLengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularLocalKeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularMandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularMaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularMinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularMustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularNamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularOrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularOrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularPathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularPatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularPositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularPrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularPresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularRangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularRequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularRevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularRevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularStatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularUniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularUnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularWhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularYangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularYinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RpcEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.TypedefEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.UndeclaredCaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.UndeclaredInputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.UndeclaredOutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;

/**
 * Static entry point to instantiating {@link EffectiveStatement} covered in the {@code RFC7950} metamodel.
 */
@Beta
@NonNullByDefault
public final class EffectiveStatements {
    private EffectiveStatements() {
        // Hidden on purpose
    }

    public static ActionEffectiveStatement copyAction(final ActionEffectiveStatement original, final Immutable path,
            final int flags) {
        checkArgument(original instanceof ActionEffectiveStatementImpl, "Unsupported original %s", original);
        return new ActionEffectiveStatementImpl((ActionEffectiveStatementImpl) original, path, flags);
    }

    public static ActionEffectiveStatement createAction(final ActionStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements)
                throws SubstatementIndexingException {
        return new ActionEffectiveStatementImpl(declared, path, flags, substatements);
    }

    public static AnydataEffectiveStatement copyAnydata(final AnydataEffectiveStatement original, final Immutable path,
            final int flags, final @Nullable AnydataSchemaNode newOriginal) {
        if (original instanceof RegularAnydataEffectiveStatement) {
            return new RegularAnydataEffectiveStatement((RegularAnydataEffectiveStatement) original, path, flags,
                newOriginal);
        } else if (original instanceof EmptyAnydataEffectiveStatement) {
            return new EmptyAnydataEffectiveStatement((EmptyAnydataEffectiveStatement) original, path, flags,
                newOriginal);
        } else {
            throw new IllegalArgumentException("Unsupported original " + original);
        }
    }

    public static AnydataEffectiveStatement createAnydata(final AnydataStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable AnydataSchemaNode original) {
        return substatements.isEmpty() ? new EmptyAnydataEffectiveStatement(declared, path, flags, original)
            : new RegularAnydataEffectiveStatement(declared, path, flags, original, substatements);
    }

    public static AnyxmlEffectiveStatement copyAnyxml(final AnyxmlEffectiveStatement original, final Immutable path,
            final int flags, final @Nullable AnyxmlSchemaNode newOriginal) {
        if (original instanceof RegularAnyxmlEffectiveStatement) {
            return new RegularAnyxmlEffectiveStatement((RegularAnyxmlEffectiveStatement) original, path, flags,
                newOriginal);
        } else if (original instanceof EmptyAnyxmlEffectiveStatement) {
            return new EmptyAnyxmlEffectiveStatement((EmptyAnyxmlEffectiveStatement) original, path, flags,
                newOriginal);
        } else {
            throw new IllegalArgumentException("Unsupported original " + original);
        }
    }

    public static AnyxmlEffectiveStatement createAnyxml(final AnyxmlStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable AnyxmlSchemaNode original) {
        return substatements.isEmpty() ? new EmptyAnyxmlEffectiveStatement(declared, path, flags, original)
            : new RegularAnyxmlEffectiveStatement(declared, path, flags, original, substatements);
    }

    public static ArgumentEffectiveStatement createArgument(final ArgumentStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyArgumentEffectiveStatement(declared)
            : new RegularArgumentEffectiveStatement(declared, substatements);
    }

    public static AugmentEffectiveStatement createAugment(final AugmentStatement declared,
            final SchemaNodeIdentifier argument, final int flags, final QNameModule rootModuleQName,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable AugmentationSchemaNode original) throws SubstatementIndexingException {
        return new AugmentEffectiveStatementImpl(declared, argument, flags, rootModuleQName, substatements, original);
    }

    public static BaseEffectiveStatement createBase(final BaseStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyBaseEffectiveStatement(declared)
            : new RegularBaseEffectiveStatement(declared, substatements);
    }

    public static BelongsToEffectiveStatement createBelongsTo(final BelongsToStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyBelongsToEffectiveStatement(declared)
            : new RegularBelongsToEffectiveStatement(declared, substatements);
    }

    public static BitEffectiveStatement createBit(final BitStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyBitEffectiveStatement(declared)
            : new RegularBitEffectiveStatement(declared, substatements);
    }

    public static CaseEffectiveStatement copyCase(final CaseEffectiveStatement original,
            final Immutable path, final int flags, final @Nullable CaseSchemaNode newOriginal) {
        if (original instanceof DeclaredCaseEffectiveStatement) {
            return new DeclaredCaseEffectiveStatement((DeclaredCaseEffectiveStatement) original, path, flags,
                newOriginal);
        } else if (original instanceof UndeclaredCaseEffectiveStatement) {
            return new UndeclaredCaseEffectiveStatement((UndeclaredCaseEffectiveStatement) original, path, flags,
                newOriginal);
        } else {
            throw new IllegalArgumentException("Unsupported origin " + original);
        }
    }

    public static CaseEffectiveStatement createCase(final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
                final @Nullable CaseSchemaNode original) {
        return new UndeclaredCaseEffectiveStatement(substatements, path, flags, original);
    }

    public static CaseEffectiveStatement createCase(final CaseStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable CaseSchemaNode original) {
        return new DeclaredCaseEffectiveStatement(declared, substatements, path, flags, original);
    }

    public static ChoiceEffectiveStatement copyChoice(final ChoiceEffectiveStatement original,
            final Immutable path, final int flags, final @Nullable ChoiceSchemaNode newOriginal) {
        checkArgument(original instanceof ChoiceEffectiveStatementImpl, "Unsupported original %s", original);
        return new ChoiceEffectiveStatementImpl((ChoiceEffectiveStatementImpl) original, path, flags, newOriginal);
    }

    public static ChoiceEffectiveStatement createChoice(final ChoiceStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable CaseSchemaNode defaultCase, final @Nullable ChoiceSchemaNode original) {
        return new ChoiceEffectiveStatementImpl(declared, substatements, path, flags, defaultCase, original);
    }

    public static ConfigEffectiveStatement createConfig(final boolean argument) {
        return argument ? EmptyConfigEffectiveStatement.TRUE : EmptyConfigEffectiveStatement.FALSE;
    }

    public static ConfigEffectiveStatement createConfig(final ConfigStatement declared) {
        return new EmptyConfigEffectiveStatement(declared);
    }

    public static ConfigEffectiveStatement createConfig(final ConfigStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createConfig(declared)
            : new RegularConfigEffectiveStatement(declared, substatements);
    }

    public static ContactEffectiveStatement createContact(final ContactStatement declared) {
        return new EmptyContactEffectiveStatement(declared);
    }

    public static ContactEffectiveStatement createContact(final ContactStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createContact(declared)
            : new RegularContactEffectiveStatement(declared, substatements);
    }

    public static ContainerEffectiveStatement copyContainer(final ContainerEffectiveStatement original,
            final Immutable path, final int flags, final @Nullable ContainerSchemaNode orig) {
        checkArgument(original instanceof ContainerEffectiveStatementImpl, "Unsupported original %s", original);
        return new ContainerEffectiveStatementImpl((ContainerEffectiveStatementImpl) original, path, flags, orig);
    }

    public static ContainerEffectiveStatement createContainer(final ContainerStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable ContainerSchemaNode original) {
        return new ContainerEffectiveStatementImpl(declared, substatements, path, flags, original);
    }

    public static DefaultEffectiveStatement createDefault(final DefaultStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyDefaultEffectiveStatement(declared)
            : new RegularDefaultEffectiveStatement(declared, substatements);
    }

    public static DescriptionEffectiveStatement createDescription(final DescriptionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyDescriptionEffectiveStatement(declared)
            : new RegularDescriptionEffectiveStatement(declared, substatements);
    }

    public static DeviateEffectiveStatement createDeviate(final DeviateStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DeviateEffectiveStatementImpl(declared, substatements);
    }

    public static DeviationEffectiveStatement createDeviation(final DeviationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DeviationEffectiveStatementImpl(declared, substatements);
    }

    public static EnumEffectiveStatement createEnum(final EnumStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyEnumEffectiveStatement(declared)
            : new RegularEnumEffectiveStatement(declared, substatements);
    }

    public static ErrorAppTagEffectiveStatement createErrorAppTag(final ErrorAppTagStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyErrorAppTagEffectiveStatement(declared)
            : new RegularErrorAppTagEffectiveStatement(declared, substatements);
    }

    public static ErrorMessageEffectiveStatement createErrorMessage(final ErrorMessageStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyErrorMessageEffectiveStatement(declared)
            : new RegularErrorMessageEffectiveStatement(declared, substatements);
    }

    public static ExtensionEffectiveStatement createExtension(final ExtensionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable SchemaPath schemaPath) {
        return new ExtensionEffectiveStatementImpl(declared, substatements, schemaPath);
    }

    public static FeatureEffectiveStatement createFeature(final FeatureStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyFeatureEffectiveStatement(declared, path, flags)
            : new RegularFeatureEffectiveStatement(declared, path, flags, substatements);
    }

    public static FractionDigitsEffectiveStatement createFractionDigits(final FractionDigitsStatement declared) {
        return new EmptyFractionDigitsEffectiveStatement(declared);
    }

    public static FractionDigitsEffectiveStatement createFractionDigits(final FractionDigitsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createFractionDigits(declared)
            : new RegularFractionDigitsEffectiveStatement(declared, substatements);
    }

    public static GroupingEffectiveStatement createGrouping(final GroupingStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final Immutable path,
            final int flags) throws SubstatementIndexingException {
        return new GroupingEffectiveStatementImpl(declared, substatements, path, flags);
    }

    public static IfFeatureEffectiveStatement createIfFeature(final IfFeatureStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyIfFeatureEffectiveStatement(declared)
            : new RegularIfFeatureEffectiveStatement(declared, substatements);
    }

    public static IdentityEffectiveStatement createIdentity(final IdentityStatement declared, final Immutable path) {
        return new EmptyIdentityEffectiveStatement(declared, path);
    }

    public static IdentityEffectiveStatement createIdentity(final IdentityStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableSet<IdentitySchemaNode> baseIdentities) {
        return new RegularIdentityEffectiveStatement(declared, path, flags, substatements, baseIdentities);
    }

    public static ImportEffectiveStatement createImport(final ImportStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final SourceIdentifier importedSource) {
        return new ImportEffectiveStatementImpl(declared, substatements, importedSource);
    }

    public static IncludeEffectiveStatement createInclude(final IncludeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyIncludeEffectiveStatement(declared)
            : new RegularIncludeEffectiveStatement(declared, substatements);
    }

    public static InputEffectiveStatement copyInput(final InputEffectiveStatement original, final Immutable path,
            final int flags) {
        if (original instanceof DeclaredInputEffectiveStatement) {
            return new DeclaredInputEffectiveStatement((DeclaredInputEffectiveStatement) original, path, flags);
        } else if (original instanceof UndeclaredInputEffectiveStatement) {
            return new UndeclaredInputEffectiveStatement((UndeclaredInputEffectiveStatement) original, path, flags);
        } else {
            throw new IllegalArgumentException("Unsupported original " + original);
        }
    }

    public static InputEffectiveStatement createInput(final Immutable path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements)
            throws SubstatementIndexingException {
        return new UndeclaredInputEffectiveStatement(substatements, path, flags);
    }

    public static InputEffectiveStatement createInput(final InputStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements)
                throws SubstatementIndexingException {
        return new DeclaredInputEffectiveStatement(declared, substatements, path, flags);
    }

    public static KeyEffectiveStatement createKey(final KeyStatement declared, final Set<QName> argument,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            return argument.equals(declared.argument()) ? new EmptyLocalKeyEffectiveStatement(declared)
                : new EmptyForeignKeyEffectiveStatement(declared, argument);
        }
        return argument.equals(declared.argument()) ? new RegularLocalKeyEffectiveStatement(declared, substatements)
            : new RegularForeignKeyEffectiveStatement(declared, argument, substatements);
    }

    public static LeafEffectiveStatement copyLeaf(final LeafEffectiveStatement original, final Immutable path,
            final int flags, final @Nullable LeafSchemaNode newOriginal) {
        checkArgument(original instanceof AbstractLeafEffectiveStatement, "Unsupported original %s", original);
        return new RegularLeafEffectiveStatement((AbstractLeafEffectiveStatement) original, path, flags, newOriginal);
    }

    public static LeafEffectiveStatement createLeaf(final LeafStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable LeafSchemaNode original)  throws SubstatementIndexingException {
        return original == null ? new EmptyLeafEffectiveStatement(declared, path, flags, substatements)
            : new RegularLeafEffectiveStatement(declared, path, flags, substatements, original);
    }

    public static LengthEffectiveStatement createLength(final LengthStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyLengthEffectiveStatement(declared)
            : new RegularLengthEffectiveStatement(declared, substatements);
    }

    public static MandatoryEffectiveStatement createMandatory(final MandatoryStatement declared) {
        return new EmptyMandatoryEffectiveStatement(declared);
    }

    public static MandatoryEffectiveStatement createMandatory(final MandatoryStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createMandatory(declared)
            : new RegularMandatoryEffectiveStatement(declared, substatements);
    }

    public static MaxElementsEffectiveStatement createMaxElements(final MaxElementsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyMaxElementsEffectiveStatement(declared)
            : new RegularMaxElementsEffectiveStatement(declared, substatements);
    }

    public static MinElementsEffectiveStatement createMinElements(final MinElementsStatement declared) {
        return new EmptyMinElementsEffectiveStatement(declared);
    }

    public static MinElementsEffectiveStatement createMinElements(final MinElementsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createMinElements(declared)
            : new RegularMinElementsEffectiveStatement(declared, substatements);
    }

    public static ModifierEffectiveStatement createModifier(final ModifierStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyModifierEffectiveStatement(declared)
            : new RegularModifierEffectiveStatement(declared, substatements);
    }

    public static MustEffectiveStatement createMust(final MustStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyMustEffectiveStatement(declared)
            : new RegularMustEffectiveStatement(declared, substatements);
    }

    public static NamespaceEffectiveStatement createNamespace(final NamespaceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyNamespaceEffectiveStatement(declared)
            : new RegularNamespaceEffectiveStatement(declared, substatements);
    }

    public static NotificationEffectiveStatement copyNotification(final NotificationEffectiveStatement original,
            final Immutable path, final int flags) {
        checkArgument(original instanceof NotificationEffectiveStatementImpl, "Unsupported original %s", original);
        return new NotificationEffectiveStatementImpl((NotificationEffectiveStatementImpl) original, path, flags);
    }

    public static NotificationEffectiveStatement createNotification(final NotificationStatement declared,
            final Immutable path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements)
                throws SubstatementIndexingException {
        return new NotificationEffectiveStatementImpl(declared, substatements, path, flags);
    }

    public static OutputEffectiveStatement copyOutput(final OutputEffectiveStatement original, final Immutable path,
            final int flags) {
        if (original instanceof DeclaredOutputEffectiveStatement) {
            return new DeclaredOutputEffectiveStatement((DeclaredOutputEffectiveStatement) original, path, flags);
        } else if (original instanceof UndeclaredOutputEffectiveStatement) {
            return new UndeclaredOutputEffectiveStatement((UndeclaredOutputEffectiveStatement) original, path, flags);
        } else {
            throw new IllegalArgumentException("Unsupported original " + original);
        }
    }

    public static OutputEffectiveStatement createOutput(final Immutable path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements)
            throws SubstatementIndexingException {
        return new UndeclaredOutputEffectiveStatement(substatements, path, flags);
    }

    public static OutputEffectiveStatement createOutput(final OutputStatement declared, final Immutable path,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements)
                throws SubstatementIndexingException {
        return new DeclaredOutputEffectiveStatement(declared, substatements, path, flags);
    }

    public static OrderedByEffectiveStatement createOrderedBy(final OrderedByStatement declared) {
        return new EmptyOrderedByEffectiveStatement(declared);
    }

    public static OrderedByEffectiveStatement createOrderedBy(final OrderedByStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createOrderedBy(declared)
            : new RegularOrderedByEffectiveStatement(declared, substatements);
    }

    public static OrganizationEffectiveStatement createOrganization(final OrganizationStatement declared) {
        return new EmptyOrganizationEffectiveStatement(declared);
    }

    public static OrganizationEffectiveStatement createOrganization(final OrganizationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createOrganization(declared)
            : new RegularOrganizationEffectiveStatement(declared, substatements);
    }

    public static PathEffectiveStatement createPath(final PathStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyPathEffectiveStatement(declared)
            : new RegularPathEffectiveStatement(declared, substatements);
    }

    public static PatternEffectiveStatement createPattern(final PatternStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyPatternEffectiveStatement(declared)
            : new RegularPatternEffectiveStatement(declared, substatements);
    }

    public static PositionEffectiveStatement createPosition(final PositionStatement declared) {
        return new EmptyPositionEffectiveStatement(declared);
    }

    public static PositionEffectiveStatement createPosition(final PositionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createPosition(declared)
            : new RegularPositionEffectiveStatement(declared, substatements);
    }

    public static PrefixEffectiveStatement createPrefix(final PrefixStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyPrefixEffectiveStatement(declared)
            : new RegularPrefixEffectiveStatement(declared, substatements);
    }

    public static PresenceEffectiveStatement createPresence(final PresenceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyPresenceEffectiveStatement(declared)
            : new RegularPresenceEffectiveStatement(declared, substatements);
    }

    public static RangeEffectiveStatement createRange(final RangeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyRangeEffectiveStatement(declared)
            : new RegularRangeEffectiveStatement(declared, substatements);
    }

    public static ReferenceEffectiveStatement createReference(final ReferenceStatement declared) {
        return new EmptyReferenceEffectiveStatement(declared);
    }

    public static ReferenceEffectiveStatement createReference(final ReferenceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createReference(declared)
            : new RegularReferenceEffectiveStatement(declared, substatements);
    }

    public static RevisionEffectiveStatement createRevision(final RevisionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyRevisionEffectiveStatement(declared)
            : new RegularRevisionEffectiveStatement(declared, substatements);
    }

    public static RequireInstanceEffectiveStatement createRequireInstance(final boolean argument) {
        return argument ? EmptyRequireInstanceEffectiveStatement.TRUE : EmptyRequireInstanceEffectiveStatement.FALSE;
    }

    public static RequireInstanceEffectiveStatement createRequireInstance(final RequireInstanceStatement declared) {
        if (EmptyRequireInstanceStatement.TRUE.equals(declared)) {
            return EmptyRequireInstanceEffectiveStatement.TRUE;
        } else if (EmptyRequireInstanceStatement.FALSE.equals(declared)) {
            return EmptyRequireInstanceEffectiveStatement.FALSE;
        } else {
            return new EmptyRequireInstanceEffectiveStatement(declared);
        }
    }

    public static RequireInstanceEffectiveStatement createRequireInstance(final RequireInstanceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createRequireInstance(declared)
            : new RegularRequireInstanceEffectiveStatement(declared, substatements);
    }

    public static RevisionDateEffectiveStatement createRevisionDate(final RevisionDateStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyRevisionDateEffectiveStatement(declared)
            : new RegularRevisionDateEffectiveStatement(declared, substatements);
    }

    public static RpcEffectiveStatement createRpc(final RpcStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final Immutable path,
                final int flags) throws SubstatementIndexingException {
        return new RpcEffectiveStatementImpl(declared, substatements, path, flags);
    }

    public static StatusEffectiveStatement createStatus(final StatusStatement declared) {
        // Aggressively reuse effective instances which are backed by the corresponding empty declared instance, as this
        // is the case unless there is a weird extension in use.
        if (EmptyStatusStatement.DEPRECATED.equals(declared)) {
            // Most likely to be seen (as current is the default)
            return EmptyStatusEffectiveStatement.DEPRECATED;
        } else if (EmptyStatusStatement.OBSOLETE.equals(declared)) {
            // less likely
            return EmptyStatusEffectiveStatement.OBSOLETE;
        } else if (EmptyStatusStatement.CURRENT.equals(declared)) {
            // ... okay, why is this there? :)
            return EmptyStatusEffectiveStatement.CURRENT;
        } else {
            return new EmptyStatusEffectiveStatement(declared);
        }
    }

    public static StatusEffectiveStatement createStatus(final StatusStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createStatus(declared)
            : new RegularStatusEffectiveStatement(declared, substatements);
    }

    public static TypedefEffectiveStatement createTypedef(final TypedefStatement declared,
            final Immutable path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypedefEffectiveStatementImpl(declared, path, flags, substatements);
    }

    public static UnitsEffectiveStatement createUnits(final UnitsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyUnitsEffectiveStatement(declared)
            : new RegularUnitsEffectiveStatement(declared, substatements);
    }

    public static UniqueEffectiveStatement createUnique(final UniqueStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyUniqueEffectiveStatement(declared)
            : new RegularUniqueEffectiveStatement(declared, substatements);
    }

    public static ValueEffectiveStatement createValue(final ValueStatement declared) {
        return new EmptyValueEffectiveStatement(declared);
    }

    public static ValueEffectiveStatement createValue(final ValueStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createValue(declared)
            : new RegularValueEffectiveStatement(declared, substatements);
    }

    public static WhenEffectiveStatement createWhen(final WhenStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyWhenEffectiveStatement(declared)
            : new RegularWhenEffectiveStatement(declared, substatements);
    }

    public static YangVersionEffectiveStatement createYangVersion(final YangVersionStatement declared) {
        if (EmptyYangVersionStatement.VERSION_1.equals(declared)) {
            return EmptyYangVersionEffectiveStatement.VERSION_1;
        } else if (EmptyYangVersionStatement.VERSION_1_1.equals(declared)) {
            return EmptyYangVersionEffectiveStatement.VERSION_1_1;
        } else {
            return new EmptyYangVersionEffectiveStatement(declared);
        }
    }

    public static YangVersionEffectiveStatement createYangVersion(final YangVersionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createYangVersion(declared)
            : new RegularYangVersionEffectiveStatement(declared, substatements);
    }

    public static YinElementEffectiveStatement createYinElement(final YinElementStatement declared) {
        return new EmptyYinElementEffectiveStatement(declared);
    }

    public static YinElementEffectiveStatement createYinElement(final YinElementStatement declared,
        final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createYinElement(declared)
            : new RegularYinElementEffectiveStatement(declared, substatements);
    }
}
