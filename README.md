# proxy-transformer
A Java 8 REST proxy that acts as a proxy for a set of endpoints and can transform requests and responses. 

## Intro

## Typical Use cases

1. You have a legacy REST endpoint without proper authentication on your local network. You can configure and deploy 
this with some security enabled. Eg Spring Security or AWS Lambda api keys. See examples
2. You need to integrate 2 (or more) RESTful services that have different apis. Sit this in the middle and configure the translations. This ought to be easier and faster than writing code.
3. You want to simplify a REST api. No problem, use the built in functions or 
write your own to simplify it by merging attributes, etc. 
4. You want the headers to appear in the body, or an attribute in the body to appear as the status code. 
5. You want to augment certain attributes, eg you could write a function that queries from a database and updates a value

## Configuration



## Compost

The proxy uses its own language and the parser is built in to keep the footprint low 
(but mainly as an interesting exercise for me!). If it is buggy or too limited, it may be replaced in future with a proper parser. 

It is called compost because it is made up of composable functions and it uses postfix notation...only joking, its called compost for more obvious reasons :) 

The typing is entirely dynamic, you need to make sure of the return value of a function before passing to another. 
Valid types are 
1. String => Surrounded by '' or without is if it is the only possibility 
2. Number => This are always interpreted as java doubles
3. Boolean (true/false)
4. List => List of one of the above

There are no Map types as such since map outputs in the body can be specified precisely in the configuration json. If this is something you need please open a PR!

## Functions available

### Basic

1. String functions
2. Number functions
3. Logical functions

### Request Functions
1. Path
2. Headers
3. Method
4. Body

### Response Functions 
Eg functions on response values from proxied service. These can only be used in the response section of the config file (not strictly enforced yet, but be careful!)
1. Body
2. Headers
3. Status Code

For the body functions, you can must use jsonPath. 

### Writing your own functions

## Examples
