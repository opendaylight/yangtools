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

identifier_stmt : IDENTIFIER string? (stmtend | (LEFT_BRACE unknown_statement* RIGHT_BRACE));
unknown_statement : (YIN_ELEMENT_KEYWORD | YANG_VERSION_KEYWORD | WHEN_KEYWORD | VALUE_KEYWORD | USES_KEYWORD | UNITS_KEYWORD | UNIQUE_KEYWORD | 
                    TYPEDEF_KEYWORD | TYPE_KEYWORD | SUBMODULE_KEYWORD | RPC_KEYWORD | REVISION_DATE_KEYWORD | REVISION_KEYWORD | 
                    REQUIRE_INSTANCE_KEYWORD | REFINE_KEYWORD | RANGE_KEYWORD | PRESENCE_KEYWORD | PREFIX_KEYWORD | 
                    POSITION_KEYWORD | PATTERN_KEYWORD | PATH_KEYWORD | OUTPUT_KEYWORD | ORGANIZATION_KEYWORD|  ORDERED_BY_KEYWORD | NOTIFICATION_KEYWORD| 
                    NAMESPACE_KEYWORD | MUST_KEYWORD | MODULE_KEYWORD | MIN_ELEMENTS_KEYWORD | MAX_ELEMENTS_KEYWORD | MANDATORY_KEYWORD | LIST_KEYWORD | 
                    LENGTH_KEYWORD | LEAF_LIST_KEYWORD | LEAF_KEYWORD | KEY_KEYWORD | INPUT_KEYWORD | INCLUDE_KEYWORD | IMPORT_KEYWORD | IF_FEATURE_KEYWORD | 
                    IDENTITY_KEYWORD | GROUPING_KEYWORD | FRACTION_DIGITS_KEYWORD | FEATURE_KEYWORD | DEVIATE_KEYWORD | DEVIATION_KEYWORD | EXTENSION_KEYWORD | 
                    ERROR_MESSAGE_KEYWORD | ERROR_APP_TAG_KEYWORD | ENUM_KEYWORD | DESCRIPTION_KEYWORD | STATUS_KEYWORD | DEFAULT_KEYWORD | CONTAINER_KEYWORD | CONTACT_KEYWORD | 
                    CONFIG_KEYWORD | CHOICE_KEYWORD |  CASE_KEYWORD | BIT_KEYWORD | BELONGS_TO_KEYWORD | BASE_KEYWORD | AUGMENT_KEYWORD |  
                    ANYXML_KEYWORD | IDENTIFIER) string? (SEMICOLON | (LEFT_BRACE (unknown_statement | identifier_stmt)* RIGHT_BRACE)*);

stmtend : (SEMICOLON) | (LEFT_BRACE identifier_stmt? RIGHT_BRACE);

/* DO NOT replace stmtsep in rest of grammar with identifier_stmt!!! It might seems as code duplicity here, but this one is necessary.
   Body of identifier_stmt generated from this grammar in YangParserListener is implemented in YangParserListenerImpl.
   To ensure that all of the identifier_stmts will be resolved correctly the YangParserListenerImpl contains code that handles
   specifcly identifier_stmts -> i.e. transforms identifier_stmt into QName. The stmtsep is used for parsing extension statements
   placed outside of body_stmt.
 */
stmtsep : IDENTIFIER string? (stmtend | (LEFT_BRACE unknown_statement* RIGHT_BRACE));
deviate_replace_stmt : DEVIATE_KEYWORD string /* REPLACE_KEYWORD */ (SEMICOLON | (LEFT_BRACE (identifier_stmt |type_stmt | units_stmt | default_stmt | config_stmt | mandatory_stmt | min_elements_stmt | max_elements_stmt )* RIGHT_BRACE));
deviate_delete_stmt : DEVIATE_KEYWORD string /* DELETE_KEYWORD */ (SEMICOLON | (LEFT_BRACE (identifier_stmt |units_stmt | must_stmt | unique_stmt | default_stmt )* RIGHT_BRACE));
deviate_add_stmt : DEVIATE_KEYWORD string /*ADD_KEYWORD*/ (SEMICOLON | (LEFT_BRACE (identifier_stmt |units_stmt | must_stmt | unique_stmt | default_stmt | config_stmt | mandatory_stmt  | min_elements_stmt  | max_elements_stmt )* RIGHT_BRACE));
deviate_not_supported_stmt : DEVIATE_KEYWORD string /*NOT_SUPPORTED_KEYWORD*/ (SEMICOLON | (LEFT_BRACE identifier_stmt? RIGHT_BRACE));
deviation_stmt : DEVIATION_KEYWORD string LEFT_BRACE (identifier_stmt |description_stmt | reference_stmt | deviate_not_supported_stmt | deviate_add_stmt | deviate_replace_stmt | deviate_delete_stmt)+ RIGHT_BRACE;
notification_stmt : NOTIFICATION_KEYWORD string (SEMICOLON | (LEFT_BRACE (identifier_stmt |if_feature_stmt | status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | data_def_stmt )* RIGHT_BRACE));
output_stmt : OUTPUT_KEYWORD LEFT_BRACE (identifier_stmt |typedef_stmt | grouping_stmt | data_def_stmt )* RIGHT_BRACE;
input_stmt : INPUT_KEYWORD LEFT_BRACE (identifier_stmt |typedef_stmt | grouping_stmt | data_def_stmt )* RIGHT_BRACE;
rpc_stmt : RPC_KEYWORD string (SEMICOLON | (LEFT_BRACE (identifier_stmt |if_feature_stmt  | status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | input_stmt | output_stmt )* RIGHT_BRACE));
when_stmt : WHEN_KEYWORD string (SEMICOLON | (LEFT_BRACE (identifier_stmt |description_stmt | reference_stmt )* RIGHT_BRACE));

augment_stmt : AUGMENT_KEYWORD string LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt | data_def_stmt | case_stmt)* RIGHT_BRACE;
uses_augment_stmt : AUGMENT_KEYWORD string LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt | data_def_stmt | case_stmt)* RIGHT_BRACE;
refine_anyxml_stmts : (identifier_stmt |must_stmt | config_stmt | mandatory_stmt | description_stmt | reference_stmt )*;
refine_case_stmts : (identifier_stmt |description_stmt | reference_stmt )*;
refine_choice_stmts : (identifier_stmt |default_stmt | config_stmt | mandatory_stmt | description_stmt | reference_stmt )*;
refine_list_stmts : (identifier_stmt |must_stmt | config_stmt | min_elements_stmt | max_elements_stmt | description_stmt | reference_stmt )*;
refine_leaf_list_stmts : (identifier_stmt |must_stmt | config_stmt | min_elements_stmt | max_elements_stmt | description_stmt | reference_stmt )*;
refine_leaf_stmts : (identifier_stmt |must_stmt | default_stmt | config_stmt | mandatory_stmt | description_stmt | reference_stmt )*;
refine_container_stmts : (identifier_stmt |must_stmt | presence_stmt | config_stmt | description_stmt | reference_stmt )*;
refine_pom : (refine_container_stmts | refine_leaf_stmts | refine_leaf_list_stmts | refine_list_stmts | refine_choice_stmts | refine_case_stmts | refine_anyxml_stmts);
refine_stmt : REFINE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (refine_pom) RIGHT_BRACE));
uses_stmt : USES_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt | refine_stmt | uses_augment_stmt )* RIGHT_BRACE));
anyxml_stmt : ANYXML_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | must_stmt | config_stmt | mandatory_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
case_stmt : CASE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt | data_def_stmt )* RIGHT_BRACE));
short_case_stmt : container_stmt | leaf_stmt | leaf_list_stmt | list_stmt | anyxml_stmt;
choice_stmt : CHOICE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | default_stmt | config_stmt | mandatory_stmt | status_stmt | description_stmt | reference_stmt | short_case_stmt | case_stmt)* RIGHT_BRACE));
unique_stmt : UNIQUE_KEYWORD string stmtend;
key_stmt : KEY_KEYWORD string stmtend;
list_stmt : LIST_KEYWORD string LEFT_BRACE  (when_stmt | if_feature_stmt | must_stmt | key_stmt | unique_stmt | config_stmt | min_elements_stmt | max_elements_stmt | ordered_by_stmt | status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | data_def_stmt | identifier_stmt)* RIGHT_BRACE;
leaf_list_stmt : LEAF_LIST_KEYWORD string LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | type_stmt | units_stmt | must_stmt | config_stmt | min_elements_stmt | max_elements_stmt | ordered_by_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE;
leaf_stmt : LEAF_KEYWORD string LEFT_BRACE  (identifier_stmt |when_stmt | if_feature_stmt | type_stmt | units_stmt | must_stmt | default_stmt | config_stmt | mandatory_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE;
container_stmt : CONTAINER_KEYWORD string (SEMICOLON | (LEFT_BRACE  (when_stmt | if_feature_stmt | must_stmt | presence_stmt | config_stmt | status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | data_def_stmt | identifier_stmt)* RIGHT_BRACE));
grouping_stmt : GROUPING_KEYWORD string (SEMICOLON | (LEFT_BRACE (status_stmt | description_stmt | reference_stmt | typedef_stmt | grouping_stmt | data_def_stmt | identifier_stmt)* RIGHT_BRACE));
value_stmt : VALUE_KEYWORD string stmtend;
max_value_arg : /*UNBOUNDED_KEYWORD |*/ string;
min_value_arg : /*UNBOUNDED_KEYWORD |*/ string;
max_elements_stmt : MAX_ELEMENTS_KEYWORD max_value_arg stmtend;
min_elements_stmt : MIN_ELEMENTS_KEYWORD min_value_arg stmtend;
error_app_tag_stmt : ERROR_APP_TAG_KEYWORD string stmtend;
error_message_stmt : ERROR_MESSAGE_KEYWORD string stmtend;
must_stmt : MUST_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |error_message_stmt | error_app_tag_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
ordered_by_arg : string; /*USER_KEYWORD | SYSTEM_KEYWORD;*/
ordered_by_stmt : ORDERED_BY_KEYWORD ordered_by_arg stmtend;
presence_stmt : PRESENCE_KEYWORD string stmtend;
mandatory_arg :string; // TRUE_KEYWORD | FALSE_KEYWORD;
mandatory_stmt : MANDATORY_KEYWORD mandatory_arg stmtend;
config_arg : string; //  TRUE_KEYWORD | FALSE_KEYWORD;
config_stmt : CONFIG_KEYWORD config_arg stmtend;
status_arg : string; /*CURRENT_KEYWORD | OBSOLETE_KEYWORD | DEPRECATED_KEYWORD; */
status_stmt : STATUS_KEYWORD status_arg stmtend;
position_stmt : POSITION_KEYWORD string stmtend;
bit_stmt : BIT_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |position_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
bits_specification : bit_stmt (bit_stmt | identifier_stmt)*;
union_specification : type_stmt (identifier_stmt | type_stmt )*;
identityref_specification : base_stmt  ;
instance_identifier_specification : (require_instance_stmt )?;
require_instance_arg :string; // TRUE_KEYWORD | FALSE_KEYWORD;
require_instance_stmt : REQUIRE_INSTANCE_KEYWORD require_instance_arg stmtend;
path_stmt : PATH_KEYWORD string stmtend;
leafref_specification : path_stmt;
enum_stmt : ENUM_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |value_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
enum_specification : enum_stmt (identifier_stmt | enum_stmt )*;
default_stmt : DEFAULT_KEYWORD string stmtend;
pattern_stmt : PATTERN_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |error_message_stmt | error_app_tag_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
length_stmt : LENGTH_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |error_message_stmt | error_app_tag_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
string_restrictions : (length_stmt | pattern_stmt )*;
fraction_digits_stmt : FRACTION_DIGITS_KEYWORD string stmtend;
decimal64_specification : (numerical_restrictions? (identifier_stmt)* fraction_digits_stmt | fraction_digits_stmt (identifier_stmt)* numerical_restrictions?);
range_stmt : RANGE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt |error_message_stmt | error_app_tag_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
numerical_restrictions : range_stmt ;
type_body_stmts : (identifier_stmt)* (numerical_restrictions | decimal64_specification | string_restrictions | enum_specification | leafref_specification | identityref_specification | instance_identifier_specification | bits_specification | union_specification) (identifier_stmt)*;
type_stmt : TYPE_KEYWORD string (SEMICOLON | (LEFT_BRACE  type_body_stmts RIGHT_BRACE));
typedef_stmt : TYPEDEF_KEYWORD string LEFT_BRACE  (identifier_stmt | type_stmt | units_stmt | default_stmt | status_stmt | description_stmt | reference_stmt )+ RIGHT_BRACE;
if_feature_stmt : IF_FEATURE_KEYWORD string stmtend;
feature_stmt : FEATURE_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt | if_feature_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
base_stmt : BASE_KEYWORD string stmtend;
identity_stmt : IDENTITY_KEYWORD string (SEMICOLON | (LEFT_BRACE  (identifier_stmt | base_stmt | status_stmt | description_stmt | reference_stmt )* RIGHT_BRACE));
yin_element_arg : string; // TRUE_KEYWORD | FALSE_KEYWORD;
yin_element_stmt : YIN_ELEMENT_KEYWORD yin_element_arg stmtend;
argument_stmt : ARGUMENT_KEYWORD string (SEMICOLON | (LEFT_BRACE identifier_stmt* yin_element_stmt? identifier_stmt* RIGHT_BRACE));
extension_stmt : EXTENSION_KEYWORD string (SEMICOLON | (LEFT_BRACE  (argument_stmt | status_stmt | description_stmt | reference_stmt | unknown_statement)* RIGHT_BRACE));
revision_date_stmt : REVISION_DATE_KEYWORD string stmtend;
revision_stmt : REVISION_KEYWORD string (SEMICOLON | (LEFT_BRACE stmtsep* (description_stmt )? (reference_stmt )? RIGHT_BRACE));
units_stmt : UNITS_KEYWORD string stmtend;
reference_stmt : REFERENCE_KEYWORD string stmtend;
description_stmt : DESCRIPTION_KEYWORD string stmtend;
contact_stmt : CONTACT_KEYWORD string stmtend;
organization_stmt : ORGANIZATION_KEYWORD string stmtend;
belongs_to_stmt : BELONGS_TO_KEYWORD string LEFT_BRACE  stmtsep* prefix_stmt  RIGHT_BRACE;
prefix_stmt : PREFIX_KEYWORD string stmtend;
namespace_stmt : NAMESPACE_KEYWORD string stmtend;
include_stmt : INCLUDE_KEYWORD string (SEMICOLON | (LEFT_BRACE stmtsep* (revision_date_stmt )? RIGHT_BRACE));
import_stmt : IMPORT_KEYWORD string LEFT_BRACE stmtsep* prefix_stmt  (revision_date_stmt )? RIGHT_BRACE;
yang_version_stmt : YANG_VERSION_KEYWORD string stmtend;
data_def_stmt : container_stmt | leaf_stmt | leaf_list_stmt | list_stmt | choice_stmt | anyxml_stmt | uses_stmt;
body_stmts : (( identifier_stmt| extension_stmt | feature_stmt | identity_stmt | typedef_stmt | grouping_stmt | data_def_stmt | augment_stmt | rpc_stmt | notification_stmt | deviation_stmt) )*;
revision_stmts :  (revision_stmt | stmtsep)*;
linkage_stmts : (import_stmt stmtsep* | include_stmt stmtsep*)*;
meta_stmts : (organization_stmt stmtsep* | contact_stmt stmtsep* | description_stmt stmtsep* | reference_stmt stmtsep*)*;
submodule_header_stmts : (yang_version_stmt stmtsep* | belongs_to_stmt stmtsep*)+ ;
module_header_stmts :  (yang_version_stmt stmtsep* | namespace_stmt stmtsep* | prefix_stmt stmtsep*)+ ;
submodule_stmt : SUBMODULE_KEYWORD string LEFT_BRACE stmtsep* submodule_header_stmts linkage_stmts meta_stmts revision_stmts body_stmts RIGHT_BRACE;
module_stmt : MODULE_KEYWORD string LEFT_BRACE stmtsep* module_header_stmts linkage_stmts meta_stmts revision_stmts body_stmts RIGHT_BRACE;