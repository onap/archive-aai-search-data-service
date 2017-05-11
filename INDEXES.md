# Document Indexes

## Overview
An index can be thought of as a collection of _Documents_, and represents the largest granularity of data grouping in the store.

The first step in persisting documents via the _Search Data Service_ is to create the _Index_ into which we will put the documents.

This is where we define the structure of the _Documents_ that we will be storing in our _Index_, including how we want the data in our documents to be analyzed and indexed so that they can be queried for in interesting and useful ways.

## Syntax
When we create an _Index_ we need to define the structure of the _Documents_ that we will be storing in it.  Specifically, we must enumerate the _Fields_ that make up the _Document_, the type of data we expect to be stored in each _Field_, and how we want that data to be indexed by the back end document store.

We express this as a JSON structure, enumerating the _Fields_ in our document, where each _Field_ is expressed as a JSON object which conforms to the following schema:
 
    {
    	"name":            {"type": "string" },
    	"data-type":       {"type": "string" },
    	"format":          {"type": "string" },
    	"searchable":      {"type": "boolean"},
    	"search-analyzer": {"type": "string" },
    	"index-analyzer":  {"type": "string" }
    }
    
Where,

    name            = An arbitrary label to assign to the _Index_
    data-type       = One of:  string, date, long, double, boolean, ip, or nested*
    format          = For 'date' type fields, the date format string to use when persisting the field.
    searchable      = true  - field will be indexed,
                      false - field will not be indexed
    search-analyzer = Default analyzer to use for queries if one is not specified as part of the query
                      One of:  whitespace or ngram.
    index-analyser  = Analyzer to use for this field when indexing documents being persisted to the Index
                      One of:  whitespace or ngram.
                    
\* **Nested** fields:
If the _data-type_ is specified as _nested_, then this indicates that the contents of the field is itself a set of document fields.  In this case, the _Field_ definition should contain an additional entry named _sub-fields_, which is a JSON array containing the definitions of the sub-fields.  

**Example - A simple document definition which includes a 'date' type field.**

_Take note of the following:_
* For our 'BirthDate' field, which is a date, we also specify the format string to use when storing the field's contents.

    {
        "fields": [
        	{"name": "FirstName", "data-type": "string"},
        	{"name": "LastName", "data-type": "string"},
        	{"name": "BirthDate", "data-type": "date", "format": "MMM d y HH:m:s"}
        ]
    }


**Example - An example document definition containing nested sub-fields.**
  
_Take note of the following:_
* It is perfectly valid for a nested field to itself contain nested fields
* For the _Tracks.Title_ field, we are specifying that the _whitespace_ analyzer should be applied for both indexing and queries. 

    {
        "fields": [
        	{"name": "Album", "data-type": "string"},
        	{"name": "Group", "data-type": "string"},
        	{"name": "Tracks", "data-type": "nested", "sub-fields": [
        		{"name": "Title", "data-type": "string", "index-analyzer": "whitespace", "search-analyzer": "whitespace"},
        		{"name": "Length", "data-type": "long"}
        	]},
        	{"name": "BandMembers", "data-type": "nested", "sub-fields": [
        		{"name": "FirstName", "data-type": "string"},
        		{"name": "LastName", "data-type": "string"},
        		{"name": "Address", "data-type": "nested", "sub-fields": [
        			{"name": "Street", "data-type": "string"},
        			{"name": "City", "data-type": "string"},
        			{"name": "Country", "data-type": "string"}
        		]}
        	]}
        ]
    }
## API

### Create Index
Define a new _Index_ in the _Search Data Service_.

---
**URL**

    https://{host}:9509/services/search-data-service/v1/search/indexes/{index}/

**Method** 

    PUT

**URL Params**

    index - The name to assign to the document index we are creating.

**Request Header**

    Accept          = application/json
    X-TransactionId = Unique id set by client (for logging purposes)
    X-FromAppId     = Application identifier (for logging purposes)
    Content-Type    = application/json
    
**Request Payload**

    JSON format document structure for this index (see Syntax Section)

**Success Response**

    Code:      201
    Header(s): None
    Body:      JSON structure containing the URL for the created Index  
               Example:
                     {"url": "indexes/myindex"}
    
**Error Response**

    400 - Bad Request
    403 - Unauthorized
    500 - Internal Error

---


### Delete Index
Remove an existing _Index_ from the _Search Data Service_.  
Note that this results in the removal of all _Documents_ that are stored in the _Index_ at the time that the DELETE operation occurs.

---
**URL**

    https://{host}:9509/services/search-data-service/v1/search/indexes/{index}/

**Method** 

    DELETE

**URL Params**

    index - The name to assign to the document index we are creating.

**Request Header**

    Accept          = application/json
    X-TransactionId = Unique id set by client (for logging purposes)
    X-FromAppId     = Application identifier (for logging purposes)
    Content-Type    = application/json

**Request Payload**

    None

**Success Response**

    Code:      201
    Header(s): None
    Body:      JSON structure containing the URL for the created Index  
               Example:
                     {"url": "indexes/myindex"}
    
**Error Response**

    400 - Bad Request
    403 - Unauthorized
    500 - Internal Error

---
