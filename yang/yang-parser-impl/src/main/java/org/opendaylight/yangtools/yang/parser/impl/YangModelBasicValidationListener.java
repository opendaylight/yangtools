/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.collect.Sets;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Anyxml_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Argument_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Augment_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Base_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Belongs_to_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Case_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Choice_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Config_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Container_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Default_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_add_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviation_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Extension_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Feature_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Grouping_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Identity_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.If_feature_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Import_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Include_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Key_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Leaf_list_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Leaf_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.List_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Mandatory_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Mandatory_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_header_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Namespace_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Notification_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Ordered_by_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Prefix_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Refine_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_date_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Rpc_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Status_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Submodule_header_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Submodule_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Type_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Typedef_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Unique_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Uses_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Yin_element_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParserBaseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation listener that validates yang statements according to RFC-6020.
 * This validator expects only one module or submodule per file and performs
 * only basic validation where context from all yang models is not present.
 */
public final class YangModelBasicValidationListener extends YangParserBaseListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(YangModelBasicValidationListener.class);
    private final Set<String> uniquePrefixes = new HashSet<>();
    private final Set<String> uniqueImports = new HashSet<>();
    private final Set<String> uniqueIncludes = new HashSet<>();

    private String globalModuleId;

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>Header statements present(mandatory prefix and namespace statements
     * are in header)</li>
     * <li>Only one module or submodule per file</li>
     * </ol>
     */
    @Override
    public void enterModule_stmt(final Module_stmtContext ctx) {

        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkPresentChildOfType(ctx, Module_header_stmtsContext.class, true);

        String moduleName = ValidationUtil.getName(ctx);
        BasicValidations.checkIsModuleIdNull(globalModuleId);
        globalModuleId = moduleName;
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>Header statements present(mandatory belongs-to statement is in
     * header)</li>
     * <li>Only one module or submodule per file</li>
     * </ol>
     */
    @Override
    public void enterSubmodule_stmt(final Submodule_stmtContext ctx) {

        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkPresentChildOfType(ctx, Submodule_header_stmtsContext.class, true);

        String submoduleName = ValidationUtil.getName(ctx);
        BasicValidations.checkIsModuleIdNull(globalModuleId);
        globalModuleId = submoduleName;

    }

    /**
     * Constraints:
     * <ol>
     * <li>One Belongs-to statement present</li>
     * </ol>
     */
    @Override
    public void enterSubmodule_header_stmts(final Submodule_header_stmtsContext ctx) {
        BasicValidations.checkPresentChildOfType(ctx, Belongs_to_stmtContext.class, true);

        // check Yang version present, if not log
        try {
            BasicValidations.checkPresentYangVersion(ctx, ValidationUtil.getRootParentName(ctx));
        } catch (Exception e) {
            LOGGER.debug(e.getMessage());
        }
    }

    /**
     * Constraints:
     * <ol>
     * <li>One Namespace statement present</li>
     * <li>One Prefix statement present</li>
     * </ol>
     */
    @Override
    public void enterModule_header_stmts(final Module_header_stmtsContext ctx) {
        String moduleName = ValidationUtil.getRootParentName(ctx);

        BasicValidations.checkPresentChildOfType(ctx, Namespace_stmtContext.class, true);
        BasicValidations.checkPresentChildOfType(ctx, Prefix_stmtContext.class, true);

        // check Yang version present, if not log
        try {
            BasicValidations.checkPresentYangVersion(ctx, moduleName);
        } catch (Exception e) {
            LOGGER.debug(e.getMessage());
        }
    }

    /**
     * Constraints:
     * <ol>
     * <li>Date is in valid format</li>
     * </ol>
     */
    @Override
    public void enterRevision_stmt(final Revision_stmtContext ctx) {
        BasicValidations.checkDateFormat(ctx);

    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>One Prefix statement child</li>
     * </ol>
     */
    @Override
    public void enterBelongs_to_stmt(final Belongs_to_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkPresentChildOfType(ctx, Prefix_stmtContext.class, true);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Namespace string can be parsed as URI</li>
     * </ol>
     */
    @Override
    public void enterNamespace_stmt(final Namespace_stmtContext ctx) {
        String namespaceName = ValidationUtil.getName(ctx);
        String rootParentName = ValidationUtil.getRootParentName(ctx);

        try {
            new URI(namespaceName);
        } catch (URISyntaxException e) {
            ValidationUtil.ex(ValidationUtil.f("(In module:%s) Namespace:%s cannot be parsed as URI", rootParentName,
                    namespaceName));
        }
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>Every import(identified by identifier) within a module/submodule is
     * present only once</li>
     * <li>One prefix statement child</li>
     * </ol>
     */
    @Override
    public void enterImport_stmt(final Import_stmtContext ctx) {

        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkUniquenessInNamespace(ctx, uniqueImports);

        BasicValidations.checkPresentChildOfType(ctx, Prefix_stmtContext.class, true);

    }

    /**
     * Constraints:
     * <ol>
     * <li>Date is in valid format</li>
     * </ol>
     */
    @Override
    public void enterRevision_date_stmt(final Revision_date_stmtContext ctx) {
        BasicValidations.checkDateFormat(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>Every include(identified by identifier) within a module/submodule is
     * present only once</li>
     * </ol>
     */
    @Override
    public void enterInclude_stmt(final Include_stmtContext ctx) {

        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkUniquenessInNamespace(ctx, uniqueIncludes);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Yang-version is specified as 1</li>
     * </ol>
     */
    @Override
    public void enterYang_version_stmt(final YangParser.Yang_version_stmtContext ctx) {
        String version = ValidationUtil.getName(ctx);
        String rootParentName = ValidationUtil.getRootParentName(ctx);
        if (!version.equals(BasicValidations.SUPPORTED_YANG_VERSION)) {
            ValidationUtil.ex(ValidationUtil.f("(In (sub)module:%s) Unsupported yang version:%s, supported version:%s",
                    rootParentName, version, BasicValidations.SUPPORTED_YANG_VERSION));
        }
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>Every prefix(identified by identifier) within a module/submodule is
     * presented only once</li>
     * </ol>
     */
    @Override
    public void enterPrefix_stmt(final Prefix_stmtContext ctx) {

        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkUniquenessInNamespace(ctx, uniquePrefixes);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>One type statement child</li>
     * </ol>
     */
    @Override
    public void enterTypedef_stmt(final Typedef_stmtContext ctx) {

        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkPresentChildOfType(ctx, Type_stmtContext.class, true);
    }

    /**
     * Constraints:
     * <ol>
     * <li>(Prefix):Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterType_stmt(final Type_stmtContext ctx) {
        BasicValidations.checkPrefixedIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterContainer_stmt(final Container_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>One type statement child</li>
     * <li>Default statement must not be present if mandatory statement is</li>
     * </ol>
     */
    @Override
    public void enterLeaf_stmt(final Leaf_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkPresentChildOfType(ctx, Type_stmtContext.class, true);

        BasicValidations.checkNotPresentBoth(ctx, Mandatory_stmtContext.class, Default_stmtContext.class);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>One type statement child</li>
     * </ol>
     */
    @Override
    public void enterLeaf_list_stmt(final Leaf_list_stmtContext ctx) {

        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkPresentChildOfType(ctx, Type_stmtContext.class, true);
    }

    private static final Set<String> PERMITTED_ORDER_BY_ARGS = Sets.newHashSet("system", "user");

    /**
     * Constraints:
     * <ol>
     * <li>Value must be one of: system, user</li>
     * </ol>
     */
    @Override
    public void enterOrdered_by_arg(final Ordered_by_argContext ctx) {
        BasicValidations.checkOnlyPermittedValues(ctx, PERMITTED_ORDER_BY_ARGS);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterList_stmt(final List_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
        // TODO check: "if config==true then key must be present" could be
        // performed
    }

    /**
     * Constraints:
     * <ol>
     * <li>No duplicate keys</li>
     * </ol>
     */
    @Override
    public void enterKey_stmt(final Key_stmtContext ctx) {
        BasicValidations.getAndCheckUniqueKeys(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>No duplicate uniques</li>
     * </ol>
     */
    @Override
    public void enterUnique_stmt(final Unique_stmtContext ctx) {
        BasicValidations.getAndCheckUniqueKeys(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * <li>Default statement must not be present if mandatory statement is</li>
     * </ol>
     */
    @Override
    public void enterChoice_stmt(final Choice_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);

        BasicValidations.checkNotPresentBoth(ctx, Mandatory_stmtContext.class, Default_stmtContext.class);

    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterCase_stmt(final Case_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
    }

    private static final Set<String> PERMITTED_BOOLEAN_ARGS = Sets.newHashSet("true", "false");

    /**
     * Constraints:
     * <ol>
     * <li>Value must be one of: true, false</li>
     * </ol>
     */
    @Override
    public void enterMandatory_arg(final Mandatory_argContext ctx) {
        BasicValidations.checkOnlyPermittedValues(ctx, PERMITTED_BOOLEAN_ARGS);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterAnyxml_stmt(final Anyxml_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterGrouping_stmt(final Grouping_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>(Prefix):Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterUses_stmt(final Uses_stmtContext ctx) {
        BasicValidations.checkPrefixedIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterRefine_stmt(final Refine_stmtContext ctx) {
        BasicValidations.checkSchemaNodeIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterRpc_stmt(final Rpc_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterNotification_stmt(final Notification_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Schema Node Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterAugment_stmt(final Augment_stmtContext ctx) {
        BasicValidations.checkSchemaNodeIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterIdentity_stmt(final Identity_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>(Prefix):Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterBase_stmt(final Base_stmtContext ctx) {
        BasicValidations.checkPrefixedIdentifier(ctx);

    }

    /**
     * Constraints:
     * <ol>
     * <li>Value must be one of: true, false</li>
     * </ol>
     */
    @Override
    public void enterYin_element_arg(final Yin_element_argContext ctx) {
        BasicValidations.checkOnlyPermittedValues(ctx, PERMITTED_BOOLEAN_ARGS);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterExtension_stmt(final Extension_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterArgument_stmt(final Argument_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterFeature_stmt(final Feature_stmtContext ctx) {
        BasicValidations.checkIdentifier(ctx);

    }

    /**
     * Constraints:
     * <ol>
     * <li>(Prefix):Identifier is in required format</li>
     * </ol>
     */
    @Override
    public void enterIf_feature_stmt(final If_feature_stmtContext ctx) {
        BasicValidations.checkPrefixedIdentifier(ctx);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Schema Node Identifier is in required format</li>
     * <li>At least one deviate-* statement child</li>
     * </ol>
     */
    @Override
    public void enterDeviation_stmt(final Deviation_stmtContext ctx) {
        BasicValidations.checkSchemaNodeIdentifier(ctx);

        Set<Class<? extends ParseTree>> types = Sets.newHashSet();
        types.add(Deviate_add_stmtContext.class);
        types.add(Deviate_add_stmtContext.class);
        BasicValidations.checkPresentChildOfTypes(ctx, types, false);
    }

    /**
     * Constraints:
     * <ol>
     * <li>Value must be one of: true, false</li>
     * </ol>
     */
    @Override
    public void enterConfig_arg(final Config_argContext ctx) {
        BasicValidations.checkOnlyPermittedValues(ctx, PERMITTED_BOOLEAN_ARGS);
    }

    private static final Set<String> PERMITTED_STATUS_ARGS = Sets.newHashSet("current", "deprecated", "obsolete");

    /**
     * Constraints:
     * <ol>
     * <li>Value must be one of: "current", "deprecated", "obsolete"</li>
     * </ol>
     */
    @Override
    public void enterStatus_arg(final Status_argContext ctx) {
        BasicValidations.checkOnlyPermittedValues(ctx, PERMITTED_STATUS_ARGS);
    }

}
