grammar SystyrantLang;

// Gramar

start: topLvlFunctions* EOF;

topLvlFunctions: importPkg | includeFile | statement;

statement: ( create=createResource | invoke=invokeMethod | define=resourceDefinition );

// Imports & Includes

importPkg: IMPORT ( packageName | fullyQualifiedId ) SC;

includeFile: INCLUDE string SC;

// Define Resource

resourceDefinition: DEF fullyQualifiedId resourceDefinitionParams? ( resourceDefinitionStatements | SC );

resourceDefinitionParams: '(' parameterAssignment* ')';

resourceDefinitionStatements: '{' statement* '}';

// Create Resource

createResource: ldep=createResource ( rlk=ARWR | llk=ARWL ) rdep=createResource | NEW elname=fullyQualifiedId params=createResourceParams? ( createResourceStatements | SC );

createResourceDepLinking: ARWR | ARWL;

createResourceStatements: '{' ( createResourceSingleInstance | createResourceMultipleInstance* ) '}';

createResourceParams: '(' parameterAssignment* ')';

createResourceSingleInstance: createResourceInstanceId? createResourceInstanceElements*;

createResourceMultipleInstance: createResourceInstanceId? createResourceInstanceElements* SC;

createResourceInstanceId: id=string COL ?;

createResourceInstanceElements: parameterAssignment | createResourceInstanceChild;

createResourceInstanceChild: createResource COMMA?;

// Invoke Method

invokeMethod: methodName=anyId '(' parameter* ')';

// Query language

query : queryExpression EOF;

queryExpression : '(' bracketExpr=queryExpression ')' | queryExpression bOp=binaryOp queryExpression | attrMatch=queryAttrMatch | co=queryChildOfMatch | tm=queryTypeMatch | id=queryIdMatch;

queryAttrMatch : AT attr=anyId ( nnul=queryAttrMatchNonNull | nul=queryAttrMatchNullOrEmpty );

queryIdMatch : staticOrDynamicValue;

queryAttrMatchNonNull : n=NOT? op=queryAttrMatchOp val=string;

queryAttrMatchNullOrEmpty : IS n=NOT? (nul=NULL | empty=EMPTY);

queryAttrMatchOp : eq=(FEQ |EQS) | lk=( EQ | LIKE ) | rgx=( REGEX | TEQ ) | m=( GT | GTS ) | l=(LT | LTS);

queryChildOfMatch : CHILDOF s=STAR? exp=queryExpression?;

queryDependsMatch : DEPENDS s=STAR? exp=queryExpression?;

queryTypeMatch : TYPE t=fullyQualifiedIdWithPkg;

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

fullyQualifiedIdWithPkg: packageName ':' anyId ;

anyId: ID | IMPORT | INCLUDE | IMPORT | NEW | AND | OR | NOT | EQS | LIKE | REGEX | IS | NULL | EMPTY | CHILDOF | TYPE | DEPENDS ;

string: astr=ASTRING | sval=staticValue;

// Lexer

AND: 'and';
OR: 'or';
NOT: 'not';
REGEX: 'regex';
TYPE: 'type';
EMPTY: 'empty';
CHILDOF: 'childof';
DEPENDS: 'depends';
IS: 'is';
NULL: 'null';
EQS: 'eq';
LIKE: 'like';
EQ: '=';
FEQ: '==';
TEQ: '~=';
ARWR: '->';
ARWL: '<-';
GT: '>';
GTS: 'gt';
LT: '<';
LTS: 'lt';
NEQ: '!=';
STAR: '*';
AT: '@';
LB: '{';
RB: '}';
LSB: '[';
RSB: ']';
COMMA: ',';
COL: ':';

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