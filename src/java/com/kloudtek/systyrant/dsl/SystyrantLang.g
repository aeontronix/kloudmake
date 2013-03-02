grammar SystyrantLang;

// Gramar

start: topLvlFunctions*;

topLvlFunctions: importPkg | includeFile | statement;

statement: ( createResource | invokeMethod | resourceDefinition );

// Imports & Includes

importPkg: IMPORT ( packageName | fullyQualifiedId ) SC;

includeFile: INCLUDE string SC;

// Define Resource

resourceDefinition: DEF fullyQualifiedId resourceDefinitionParams? ( '{' statement* '}' )?;

resourceDefinitionParams: '(' parameterAssignment* ')';

// Create Resource

createResource: NEW elname=fullyQualifiedId params=createResourceParams? '{' ( createResourceSingleInstance | createResourceMultipleInstance* ) '}';

createResourceParams: '(' parameterAssignment* ')';

createResourceSingleInstance: createResourceInstanceId? createResourceInstanceElements*;

createResourceMultipleInstance: createResourceInstanceId? createResourceInstanceElements* SC;

createResourceInstanceId: staticOrDynamicValue '=>'?;

createResourceInstanceElements: parameterAssignment | createResourceInstanceChild;

createResourceInstanceChild: createResource COMMA?;

// Invoke Method

invokeMethod: methodName=anyId '(' parameter* ')';

// Common

parameter: ( anyId EQ )? staticOrDynamicValue COMMA?;

parameterAssignment: paramName=anyId EQ staticOrDynamicValue COMMA?;

staticValue: QSTRING | UQSTRING | NB | anyId;

staticOrDynamicValue: staticValue | dynamicValue | invokeMethod;

dynamicValue: ASTRING | variableLookupValue;

variableLookupValue: '$' anyId;

packageName: anyId ( '.' anyId )*;

fullyQualifiedId: ( packageName ':' )? anyId ;

anyId: ID | IMPORT | INCLUDE | IMPORT | NEW;

string: ASTRING | QSTRING | UQSTRING | NB | anyId;

// Lexer

EQ: '=';
LB: '{';
RB: '}';
LSB: '[';
RSB: ']';
COMMA: ',';

INCLUDE : 'include';
DEF : 'def';
NEW : 'new';
SC : ';';
IMPORT : 'import';
LF: '\n' -> skip;
WS: [ \r\t]+ -> skip;
NB: ([0-9] | '.')+;
ID: [a-zA-Z] [a-zA-Z0-9-_]*;
UQSTRING: [a-zA-Z0-9-_/$%^&*!];
QSTRING: '\'' ( ESCAPE | ~('\''|'\\') )* '\'';
ASTRING: '"' ( ESCAPE | ~('"'|'\\') )* '"';
fragment
ESCAPE : '\\' . ;