//
// Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
parser grammar YangParser;

@header {
package org.opendaylight.yangtools.antlrv4.code.gen;
}

options{
    tokenVocab=YangLexer;
    
}


yang : module_stmt | submodule_stmt ;

string : STRING (PLUS STRING)*;

// string validated in YangParserListenerImpl.handleUnknownNode()
identifier_stmt : IDENTIFIER string? (stmtend | (LEFT_BRACE unknown_statement* RIGHT_BRACE));
// string validated in YangParserListenerImpl.handleUnknownNode()
unknown_statement : (YIN_ELEMENT_KEYWORD | YANG_VERSION_KEYWORD | WHEN_KEYWORD | VALUE_KEYWORD | USES_KEYWORD | UNITS_KEYWORD | UNIQUE_KEYWORD |
                    TYPEDEF_KEYWORD | TYPE_KEYWORD | SUBMODULE_KEYWORD | RPC_KEYWORD | REVISION_DATE_KEYWORD | REVISION_KEYWORD | 
                    REQUIRE_INSTANCE_KEYWORD | REFINE_KEYWORD | RANGE_KEYWORD | PRESENCE_KEYWORD | PREFIX_KEYWORD | 
                    POSITION_KEYWORD | PATTERN_KEYWORD | PATH_KEYWORD | OUTPUT_KEYWORD | ORGANIZATION_KEYWORD|  ORDERED_BY_KEYWORD | NOTIFICATION_KEYWORD| 
                    NAMESPACE_KEYWORD | MUST_KEYWORD | MODULE_KEYWORD | MIN_ELEMENTS_KEYWORD | MAX_ELEMENTS_KEYWORD | MANDATORY_KEYWORD | LIST_KEYWORD | 
                    LENGTH_KEYWORD | LEAF_LIST_KEYWORD | LEAF_KEYWORD | KEY_KEYWORD | INPUT_KEYWORD | INCLUDE_KEYWORD | IMPORT_KEYWORD | IF_FEATURE_KEYWORD | 
                    IDENTITY_KEYWORD | GROUPING_KEYWORD | FRACTION_DIGITS_KEYWORD | FEATURE_KEYWORD | DEVIATE_KEYWORD | DEVIATION_KEYWORD | EXTENSION_KEYWORD | 
                    ERROR_MESSAGE_KEYWORD | ERROR_APP_TAG_KEYWORD | ENUM_KEYWORD | DESCRIPTION_KEYWORD | STATUS_KEYWORD | DEFAULT_KEYWORD | CONTAINER_KEYWORD | CONTACT_KEYWORD | 
                    CONFIG_KEYWORD | CHOICE_KEYWORD |  CASE_KEYWORD | BIT_KEYWORD | BELONGS_TO_KEYWORD | BASE_KEYWORD | AUGMENT_KEYWORD |  
                    ANYXML_KEYWORD | REFERENCE_KEYWORD | IDENTIFIER) string? (SEMICOLON | (LEFT_BRACE
                    (unknown_statement |
                    identifier_stmt)* RIGHT_BRACE)*);

stmtend : (SEMICOLON) | (LEFT_BRACE identifier_stmt? RIGHT_BRACE);

/* DO NOT replace stmtsep in rest of grammar with identifier_stmt!!! It might seems as code duplicity here, but this one is necessary.
   Body of identifier_stmt generated from this grammar in YangParserListener is implemented in YangParserListenerImpl.
   To ensure that all of the identifier_stmts will be resolved correctly the YangParserListenerImpl contains code that handles
   specifcly identifier_stmts -> i.e. transforms identifier_stmt into QName. The stmtsep is used for parsing extension statements
   placed outside of body_stmt.
 */
stmtsep : IDENTIFIER string? (stmtend | (LEFT_BRACE unknown_statement* RIGHT_BRACE));
// string validated in DeviationBuilder.setDeviate() as REPLACE_KEYWORD
deviate_replace_stmt : DEVIATE_KEYWORD string (SEMICOLON | (LEFT_BRACE (identifier_stmt |type_stmt | units_stmt | default_stmt | config_stmt | mandatory_stmt | min_elements_stmt | max_elements_stmt )* RIGHT_BRACE));
// string validated in DeviationBuilder.setDeviate() as DELETE_KEYWORD
deviate_delete_stmt : DEVIATE_KEYWORD string (SEMICOLON | (LEFT_BRACE (identifier_stmt |units_stmt | must_stmt | unique_stmt | default_stmt )* RIGHT_BRACE));
// string validated in DeviationBuilder.setDeviate() as ADD_KEYWORD
deviate_add_stmt : DEVIATE_KEYWORD string (SEMICOLON | (LEFT_BRACE (identifier_stmt |units_stmt | must_stmt | unique_stmt | default_stmt | config_stmt | mandatory_stmt  | min_elements_stmt  | max_elements_stmt )* RIGHT_BRACE));
// string validated in DeviationBuilder.setDeviate() as NOT_SUPPORTED_KEYWORD
deviate_not_supported_stmt : DEVIATE_KEYWORD string (SEMICOLON | (LEFT_BRACE identifier_stmt? RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterDeviation_stmt()
deviation_stmt : DEVIATION_KEYWORD string LEFT_BRACE (identifier_stmt |description_stmt | reference_stmt | deviate_not_supported_stmt | deviate_add_stmt | deviate_replace_stmt | deviate_delete_stmt)+ RIGHT_BRACE;
// string validated in YangModelBasicValidationListener.enterNotification_stmt()
notification_stmt : NOTIFICATION_KEYWORD string (SEMICOLON | (LEFT_BRACE (identifier_stmt |if_feature_stmt | status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | data_def_stmt )* RIGHT_BRACE));
output_stmt : OUTPUT_KEYWORD LEFT_BRACE (identifier_stmt |typedef_stmt | grouping_stmt | data_def_stmt )* RIGHT_BRACE;
input_stmt : INPUT_KEYWORD LEFT_BRACE (identifier_stmt |typedef_stmt | grouping_stmt | data_def_stmt )* RIGHT_BRACE;
// string valided in YangModelBasicValidationListener.enterRpc_stmt()
rpc_stmt : RPC_KEYWORD string (SEMICOLON | (LEFT_BRACE (identifier_stmt |if_feature_stmt  | status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | input_stmt | output_stmt )* RIGHT_BRACE));
// string validated in ParserListenerUtils.stringFromStringContext()
when_stmt : WHEN_KEYWORD string (SEMICOLON | (LEFT_BRACE (identifier_stmt |description_stmt | reference_stmt )* RIGHT_BRACE));

// string validated in YangModelBasicValidationListener.enterAugment_stmt()
augment_stmt : AUGMENT_KEYWORD string LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt | data_def_stmt | case_stmt)* RIGHT_BRACE;
// string not validated
uses_augment_stmt : AUGMENT_KEYWORD string LEFT_BRACE (identifier_stmt |when_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt | data_def_stmt | case_stmt)* RIGHT_BRACE;
refine_anyxml_stmts : (identifier_stmt |must_stmt | config_stmt | mandatory_stmt | description_stmt | reference_stmt )*;
refine_case_stmts : (identifier_stmt |description_stmt | reference_stmt )*;
refine_choice_stmts : (identifier_stmt |default_stmt | config_stmt | mandatory_stmt | description_stmt | reference_stmt )*;
refine_list_stmts : (identifier_stmt |must_stmt | config_stmt | min_elements_stmt | max_elements_stmt | description_stmt | reference_stmt )*;
refine_leaf_list_stmts : (identifier_stmt |must_stmt | config_stmt | min_elements_stmt | max_elements_stmt | description_stmt | reference_stmt )*;
refine_leaf_stmts : (identifier_stmt |must_stmt | default_stmt | config_stmt | mandatory_stmt | description_stmt | reference_stmt )*;
refine_container_stmts : (identifier_stmt |must_stmt | presence_stmt | config_stmt | description_stmt | reference_stmt )*;
refine_pom : (refine_container_stmts | refine_leaf_stmts | refine_leaf_list_stmts | refine_list_stmts | refine_choice_stmts | refine_case_stmts | refine_anyxml_stmts);
// string validated in YangModelBasicValidationListener.enterRefine_stmt()
refine_stmt : REFINE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (refine_pom) RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterUses_stmt()
uses_stmt : USES_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt | refine_stmt | uses_augment_stmt )* RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterAnyxml_stmt()
anyxml_stmt : ANYXML_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | must_stmt | config_stmt | mandatory_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterCase_stmt()
case_stmt : CASE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt | data_def_stmt )* RIGHT_BRACE));
short_case_stmt : container_stmt | leaf_stmt | leaf_list_stmt | list_stmt | anyxml_stmt;
// string validated in YangModelBasicValidationListener.enterChoice_stmt()
choice_stmt : CHOICE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | default_stmt | config_stmt | mandatory_stmt | status_stmt | description_stmt | reference_stmt | short_case_stmt | case_stmt)* RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterUnique_stmt()
unique_stmt : UNIQUE_KEYWORD string stmtend;
// string validated in YangModelBasicValidationListener.enterKey_stmt()
key_stmt : KEY_KEYWORD string stmtend;
// string validated in YangModelBasicValidationListener.enterList_stmt()
list_stmt : LIST_KEYWORD string LEFT_BRACE  (when_stmt | if_feature_stmt | must_stmt | key_stmt | unique_stmt | config_stmt | min_elements_stmt | max_elements_stmt | ordered_by_stmt | status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | data_def_stmt | identifier_stmt)* RIGHT_BRACE;
// string validated in YangModelBasicValidationListener.enterLeaf_list_stmt()
leaf_list_stmt : LEAF_LIST_KEYWORD string LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | type_stmt | units_stmt | must_stmt | config_stmt | min_elements_stmt | max_elements_stmt | ordered_by_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE;
// string validated in YangModelBasicValidationListener.enterLeaf_stmt()
leaf_stmt : LEAF_KEYWORD string LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | type_stmt | units_stmt | must_stmt | default_stmt | config_stmt | mandatory_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE;
// string validated in YangModelBasicValidationListener.enterContainer_stmt()
container_stmt : CONTAINER_KEYWORD string (SEMICOLON | (LEFT_BRACE  (when_stmt | if_feature_stmt | must_stmt | presence_stmt | config_stmt | status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | data_def_stmt | identifier_stmt)* RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterGrouping_stmt()
grouping_stmt : GROUPING_KEYWORD string (SEMICOLON | (LEFT_BRACE (status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | data_def_stmt | identifier_stmt)* RIGHT_BRACE));
// string validated in ParserListenerUtils.createEnumPair()
value_stmt : VALUE_KEYWORD string stmtend;
// string validated in ParserListenerUtils.parseMaxElements()
max_value_arg : string;
// string validated in ParserListenerUtils.parseMinElements()
min_value_arg : string;
max_elements_stmt : MAX_ELEMENTS_KEYWORD max_value_arg stmtend;
min_elements_stmt : MIN_ELEMENTS_KEYWORD min_value_arg stmtend;
// string validated in ParserListenerUtils.parseMust()
error_app_tag_stmt : ERROR_APP_TAG_KEYWORD string stmtend;
// string validated in ParserListenerUtils.parseMust()
error_message_stmt : ERROR_MESSAGE_KEYWORD string stmtend;
// string validated in ParserListenerUtils.parseMust()
must_stmt : MUST_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |error_message_stmt | error_app_tag_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterOrdered_by_arg() as USER_KEYWORD | SYSTEM_KEYWORD
ordered_by_arg : string;
ordered_by_stmt : ORDERED_BY_KEYWORD ordered_by_arg stmtend;
// string not validated
presence_stmt : PRESENCE_KEYWORD string stmtend;
// string validated in YangModelBasicValidationListener.enterMandatory_arg() as TRUE_KEYWORD | FALSE_KEYWORD
mandatory_arg :string;
mandatory_stmt : MANDATORY_KEYWORD mandatory_arg stmtend;
// string validated in YangModelBasicValidationListener.enterConfig_arg() as TRUE_KEYWORD | FALSE_KEYWORD
config_arg : string;
config_stmt : CONFIG_KEYWORD config_arg stmtend;
// string validated in YangModelBasicValidationListener.enterStatus_arg() as CURRENT_KEYWORD | OBSOLETE_KEYWORD | DEPRECATED_KEYWORD
status_arg : string;
status_stmt : STATUS_KEYWORD status_arg stmtend;
// string validated in ParserListenerUtils.parseBit()
position_stmt : POSITION_KEYWORD string stmtend;
// string validated in ParserListenerUtils.parseBit()
bit_stmt : BIT_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |position_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
bits_specification : bit_stmt (bit_stmt | identifier_stmt)*;
union_specification : type_stmt (identifier_stmt | type_stmt )*;
identityref_specification : base_stmt  ;
instance_identifier_specification : (require_instance_stmt )?;
// string validated in ParserListenerUtils.isRequireInstance() as TRUE_KEYWORD | FALSE_KEYWORD
require_instance_arg :string;
require_instance_stmt : REQUIRE_INSTANCE_KEYWORD require_instance_arg stmtend;
// string validated in ParserListenerUtils.parseLeafrefPath()
path_stmt : PATH_KEYWORD string stmtend;
leafref_specification : path_stmt;
// string validated in ParserListenerUtils.createEnumPair()
enum_stmt : ENUM_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |value_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
enum_specification : enum_stmt (identifier_stmt | enum_stmt )*;
// string not validated
default_stmt : DEFAULT_KEYWORD string stmtend;
// string validated in ParserListenerUtils.parsePatternConstraint()
pattern_stmt : PATTERN_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |error_message_stmt | error_app_tag_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
// string validated in ParserListenerUtils.parseLengthConstraints()
length_stmt : LENGTH_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |error_message_stmt | error_app_tag_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
string_restrictions : (length_stmt | pattern_stmt )*;
// string validated in ParserListenerUtils.parseFractionDigits()
fraction_digits_stmt : FRACTION_DIGITS_KEYWORD string stmtend;
decimal64_specification : (numerical_restrictions? (identifier_stmt)* fraction_digits_stmt | fraction_digits_stmt (identifier_stmt)* numerical_restrictions?);
// string validated in ParserListenerUtils.parseRangeConstraints()
range_stmt : RANGE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |error_message_stmt | error_app_tag_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
numerical_restrictions : range_stmt ;
type_body_stmts : (identifier_stmt)* (numerical_restrictions | decimal64_specification | string_restrictions | enum_specification | leafref_specification | identityref_specification | instance_identifier_specification | bits_specification | union_specification) (identifier_stmt)*;
// string validated in YangModelBasicValidationListener.enterType_stmt()
type_stmt : TYPE_KEYWORD string (SEMICOLON | (LEFT_BRACE  type_body_stmts RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterTypedef_stmt()
typedef_stmt : TYPEDEF_KEYWORD string LEFT_BRACE  (identifier_stmt | type_stmt | units_stmt | default_stmt | status_stmt | description_stmt | reference_stmt )+ RIGHT_BRACE;
// string validated in YangModelBasicValidationListener.enterIf_feature_stmt()
if_feature_stmt : IF_FEATURE_KEYWORD string stmtend;
// string validated in YangModelBasicValidationListener.enterFeature_stmt()
feature_stmt : FEATURE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterBase_stmt()
base_stmt : BASE_KEYWORD string stmtend;
// string validated in YangModelBasicValidationListener.enterIdentity_stmt()
identity_stmt : IDENTITY_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt | base_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterYin_element_arg() as TRUE_KEYWORD | FALSE_KEYWORD
yin_element_arg : string;
yin_element_stmt : YIN_ELEMENT_KEYWORD yin_element_arg stmtend;

// string validated in YangModelBasicValidationListener.enterArgument_stmt()
argument_stmt : ARGUMENT_KEYWORD string (SEMICOLON | (LEFT_BRACE identifier_stmt* yin_element_stmt? identifier_stmt* RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterExtension_stmt()
extension_stmt : EXTENSION_KEYWORD string (SEMICOLON | (LEFT_BRACE  (argument_stmt | status_stmt | description_stmt | reference_stmt | unknown_statement)* RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterRevision_date_stmt()
revision_date_stmt : REVISION_DATE_KEYWORD string stmtend;
// string validated in YangModelBasicValidationListener.enterRevision_stmt()
revision_stmt : REVISION_KEYWORD string (SEMICOLON | (LEFT_BRACE stmtsep* (description_stmt )? (reference_stmt )? (unknown_statement)? RIGHT_BRACE));
// string not validated
units_stmt : UNITS_KEYWORD string stmtend;
// string not validated
reference_stmt : REFERENCE_KEYWORD string stmtend;
// string not validated
description_stmt : DESCRIPTION_KEYWORD string stmtend;
// string not validated
contact_stmt : CONTACT_KEYWORD string stmtend;
// string not validated
organization_stmt : ORGANIZATION_KEYWORD string stmtend;
// string validated in YangModelBasicValidationListener.enterBelongs_to_stmt()
belongs_to_stmt : BELONGS_TO_KEYWORD string LEFT_BRACE  stmtsep* prefix_stmt  RIGHT_BRACE;
// string validated in YangModelBasicValidationListener.enterPrefix_stmt()
prefix_stmt : PREFIX_KEYWORD string stmtend;
// string validated in YangModelBasicValidationListener.enterNamespace_stmt()
namespace_stmt : NAMESPACE_KEYWORD string stmtend;
// string validated in YangModelBasicValidationListener.enterInclude_stmt()
include_stmt : INCLUDE_KEYWORD string (SEMICOLON | (LEFT_BRACE stmtsep* (revision_date_stmt )? RIGHT_BRACE));
// string validated in YangModelBasicValidationListener.enterImport_stmt()
import_stmt : IMPORT_KEYWORD string LEFT_BRACE stmtsep* prefix_stmt  (revision_date_stmt )? RIGHT_BRACE;
// string validated in YangModelBasicValidationListener.enterModule_header_stmts() and/or YangModelBasicValidationListener.enterSubmodule_header_stmts()
yang_version_stmt : YANG_VERSION_KEYWORD string stmtend;
data_def_stmt : container_stmt | leaf_stmt | leaf_list_stmt | list_stmt | choice_stmt | anyxml_stmt | uses_stmt;
body_stmts : (( identifier_stmt| extension_stmt | feature_stmt | identity_stmt | typedef_stmt | grouping_stmt | data_def_stmt | augment_stmt | rpc_stmt | notification_stmt | deviation_stmt) )*;
revision_stmts :  (revision_stmt | stmtsep)*;
linkage_stmts : (import_stmt stmtsep* | include_stmt stmtsep*)*;
meta_stmts : (organization_stmt stmtsep* | contact_stmt stmtsep* | description_stmt stmtsep* | reference_stmt stmtsep*)*;
submodule_header_stmts : (yang_version_stmt stmtsep* | belongs_to_stmt stmtsep*)+ ;
module_header_stmts :  (yang_version_stmt stmtsep* | namespace_stmt stmtsep* | prefix_stmt stmtsep*)+ ;
// string validated in YangModelBasicValidationListener.enterSubmodule_stmt()
submodule_stmt : SUBMODULE_KEYWORD string LEFT_BRACE stmtsep* submodule_header_stmts linkage_stmts meta_stmts revision_stmts body_stmts RIGHT_BRACE;
// string validated in YangModelBasicValidationListener.enterModule_stmt()
module_stmt : MODULE_KEYWORD string LEFT_BRACE stmtsep* module_header_stmts linkage_stmts meta_stmts revision_stmts body_stmts RIGHT_BRACE;
