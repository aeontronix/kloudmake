grammar SystyrantLang;

// Gramar

start: topLvlFunctions* EOF;

topLvlFunctions: importPkg | includeFile | statement;

statement: ( createResource | invokeMethod | resourceDefinition );

// Imports & Includes

importPkg: IMPORT ( packageName | fullyQualifiedId ) SC;

includeFile: INCLUDE string SC;

// Define Resource

resourceDefinition: DEF fullyQualifiedId resourceDefinitionParams? ( resourceDefinitionStatements | SC );

resourceDefinitionParams: '(' parameterAssignment* ')';

resourceDefinitionStatements: '{' statement* '}';

// Create Resource

createResource: NEW elname=fullyQualifiedId params=createResourceParams? ( createResourceStatements | SC );

createResourceStatements: '{' ( createResourceSingleInstance | createResourceMultipleInstance* ) '}';

createResourceParams: '(' parameterAssignment* ')';

createResourceSingleInstance: createResourceInstanceId? createResourceInstanceElements*;

createResourceMultipleInstance: createResourceInstanceId? createResourceInstanceElements* SC;

createResourceInstanceId: staticOrDynamicValue '=>'?;

createResourceInstanceElements: parameterAssignment | createResourceInstanceChild;

createResourceInstanceChild: createResource COMMA?;

// Invoke Method

invokeMethod: methodName=anyId '(' parameter* ')';

// Query language

query : queryExpression EOF;

queryExpression : '(' bracketExpr=queryExpression ')' | queryExpression bOp=binaryOp queryExpression | attrMatch=queryAttrMatch;

queryAttrMatch : AT attr=anyId ( nnul=queryAttrMatchNonNull | nul=queryAttrMatchNullOrEmpty );

queryAttrMatchNonNull : n=NOT? op=queryAttrMatchOp val=string;

queryAttrMatchNullOrEmpty : IS n=NOT? (nul=NULL | empty=EMPTY);

queryAttrMatchOp : eq=EQS | lk=LIKE | rgx=REGEX;

// Common

logOp : EQ | NEQ;

binaryOp : a=AND | o=OR;

parameter: ( anyId EQ )? staticOrDynamicValue COMMA?;

parameterAssignment: paramName=anyId EQ staticOrDynamicValue COMMA?;

staticValue: qstr=QSTRING | uqstr=UQSTRING | nb=NB | id=anyId;

staticOrDynamicValue: staticValue | dynamicValue | invokeMethod;

dynamicValue: ASTRING | variableLookupValue;

variableLookupValue: '$' anyId;

packageName: anyId ( '.' anyId )*;

fullyQualifiedId: ( packageName ':' )? anyId ;

anyId: ID | IMPORT | INCLUDE | IMPORT | NEW | AND | OR | NOT | EQS | LIKE | REGEX | IS | NULL | EMPTY;

string: astr=ASTRING | sval=staticValue;

// Lexer

AND: 'and';
OR: 'or';
NOT: 'not';
REGEX: 'regex';
EMPTY: 'empty';
IS: 'is';
NULL: 'null';
EQS: 'eq';
LIKE: 'like';
EQ: '=';
AT: '@';
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