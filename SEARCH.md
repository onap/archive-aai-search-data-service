# Search Requests

## Overview
Ultimately, the point of storing documents in a document store is to be able to access the data embodied in those documents in a variety of interesting ways.  To put it another way, we want to be able to ask interesting questions of the data that has been persisted, not just get back a document that we put in.

We do this by submitting _Search Requests_ to the _Search Service_.

Conceptually, the structure of a _Search Request_ can be visualized as a pipeline consisting of three phases of operations that are applied to our document set in order to produce a query result set:

    +---------------------+
    | ENTIRE DOCUMENT SET | ----------------------------- We begin with the entire set of documents in our index.
    +---------------------+
               |
               V
        \-------------/          
         \ Filter(s) / ---------------------------------- We optionally apply a set of filter criteria to the document 
          -----------                                     set, with the intent of reducing the overall set of documents 
               |                                          that we will be applying our query against (filtering is cheaper 
               |                                          than running the full queries)
               V                   
      +-----------------+        
      | DOCUMENT SUBSET | ------------------------------- Our filter stage produces a (hopefully) smaller subset of documents 
      +-----------------+                                 than we started with.
               |
               V
         \-----------/      
          \ Queries / ----------------------------------- Now we execute our queries against the filtered document subset.
           --------- 
               |
               V
     +------------------+          
     | QUERY RESULT SET | ------------------------------- This produces a scored set of results. 
     +------------------+            
               |                     
               V                                
      \----------------/             
       \ Aggregations / --------------------------------- Optionally we may apply a set of aggregations against the query               
        --------------                                    result set.
               |                     
               V                     
    +---------------------+          
    | AGGREGATION BUCKETS | ----------------------------- This produces a set of aggregation buckets based on the query
    +---------------------+                               result set.



### Filters
_Filters_ are intended to answer the following question about the documents that they are applied to:  _Does this document match the specified criteria?_

In other words, filter queries produce **yes/no** results.  A given document either **IS** or **IS NOT** in the set of documents that we want.  This can also be described as a _non-scored_ search and is typically a cheaper operation than performing a _scored_ search (more on those below).  This is why we often want to pre-filter our document set before applying more expensive query operations.

### Queries
_Queries_ are intended to answer the following question about the documents that they are applied to: _How well does this document match the specified criteria?_ 

In other words, the criteria which we include in our _Query_ is not intended to produce a set of _yes/no_ results, but a _scored_ result set which includes the set of documents that meet some combination of the criteria which we supply and includes a _score_ indicating _how well_ a particular document meets the overall requirement set.  The more criteria that a particular document meets, the higher its score.

### Aggregations
_Aggregations_ are intended to answer questions that summarize and analyze an overall data set (typically a _Query_ result set).

_Aggregations_ produce result sets that group the data set into buckets, based on the _Aggregation_ criteria, and allow questions such as the following to be asked:

* Of all of the people in my result set, how many are taller than 5' 10"?
* How many different server vendors have hardware installed in my network?
* What proportion of employees serviced by our IT department use Macs?  What proportion use Windows?

## Syntax

### The Filter Stanza

If you intend for your _Search Request_ to include a _Filter_ stage, then you must define the _filter queries_ that you wish to be applied in the _Filter Stanza_ of your _Search Request_.  The following pseudo-json illustrates the structure of the _Filter Stanza_:

    {
        "filter": {
            "all": [ {filter-query}, {filter-query}, ... {filter-query}],
            "any": [ {filter-query}, {filter-query}, ... {filter-query}]
        }
    }

As we can see, our _Filter Stanza_ consists of two optional groupings of _query_ statements: _any_ and _all_.  These groupings will produce the following behaviours:

* _all_ - _Documents_ must match ALL of the _queries_ in this grouping in order to be included in the result set.
* _any_ - _Documents_ must match a minimum of ONE of the _queries_ in this grouping in order to be included in the result set.

The _filter-queries_ themselves are syntactically identical to the _Queries_ which we will define below, the only difference being that, because they are declared within the _Filter Stanza_ they will be applied as _unscored_ queries.

### The Query Stanza

The _Query Stanza_ is where we define _query statements_ which are intended to produce _scored results_, as opposed to _queries_ which are intended for filtering purposes.

The _Query Stanza_ is expressed as a list of _query statements_, each of which is prefixed with a directive indicating how strongly the _query_ in question is to be applied.

_Queries_ prefixed with the "must" directive, represent queries which all documents must satisfy to be included in the result set, whereas, _queries_ prefixed with the "may" directive represent queries which are not required for a document to be included in the result set, although they will score higher if they do.

The following pseudo-json illustrates the structure of the _Query Stanza_:
 
    {
        "queries": [
            { "must": { <query> } },
            { "must": { <query> } },
                 .
                 .
            { "must": { <query> } },
            { "may" : { <query> } },
                 .
                 .
            { "may" : { <query> } }
        ]
    }

**Nested Fields** - If the document to be queried contains nested fields, then these may be referenced by providing the fully qualified field name using dotted notation.  Example: _myFieldName.mySubFieldName_

** Result-Set Restrictions** - In some cases, where the number of hits for a given set of queries is expected to be large, the client may want to restrict the size of the result set that is returned, as well as manipulate which subset of results it gets back.  This can be accomplished with the following optional fields in the Search Statement:
* results-start - Index into the result set to the first document to be returned.
* results-size - The maximum number of documents to be returned in the result set.

Both of these fields are optional - leaving them out implies that the entire result set should be returned to the client.

**IMPORTANT - Note that although the these two fields may be used by the client to get back query results a chunk at a time by resending the same query repeatedly and specifying a different 'results-start' value each time, be aware that this is NOT a transactional operation.  There is no guarantee that changes to the underlying data may not occur in between query calls.  This is NOT intended to be the equivalent of a mechanism such as the 'Scroll API' provided by ElasticSearch or a cursor in a traditional data base.**

We will discuss the specific query types supported below, and then provide some concrete examples.

**Term Query**

A _Term_ query attempts to match the literal value of a field, with no advanced parsing or analysis of the query string.

There are two operations supported by the _Term_ query type:
* match - The contents of the specified field must match the supplied value.
* not-match - The contents of the specified field must NOT match the supplied value.

_Example - Simple Match_

    {
        "match": { "FirstName": "Bob" }
    }
    
_Example - Simple Not-Match_

    {
        "not-match": { "LastName": "Smith" }
    }
    
Note that the term match is applied against the tokenized field contents.

For example, if a field containing the contents: "_the quick brown fox_" was analyzed with a white space tokenizer (this occurs on document creation or update), the following inverted indexes would be created for the field at the time of document insertion:

    the
    quick
    brown
    fox

Meaning that the following term queries would all produce a match for our document:

         {"must": {"match": { "my-field-name": "the"}}}
         {"must": {"match": { "my-field-name": "quick"}}}
         {"must": {"match": { "my-field-name": "brown"}}}
         {"must": {"match": { "my-field-name": "fox"}}}

         
**Multi Field Term Query**

A variant of the _Term_ query described above is the  _Multi Field Term Query_, which, as the name suggests, allows for a _Term_ query to be applied across multiple fields.  The syntax is the same as for a single field term query except that the fields are supplied as a space-delimited list, as in the example below:

    {"must": {"match": {"field": "field1 field2 field3", "value": "blah"}}}

The above query would produce a hit for any documents containing the value "blah" in any of "field1", "field2", or "field3".
 
Note that it is also valid to supply multiple values in the same manner, by supplying a space-delimited list in the "value" field.

The default behaviour in this case is to produce a hit only if there is at least one occurrence of EVERY supplied value in any of the specified fields.

For example, the following query:

    {"must": {"match": {"field": "first_name last_name", "value": "Smith Will"}}}
  
Produces a match for document {"first_name": "Will", "last_name": "Smith"} but not {"first_name": "Will", "last_name": "Shakespeare"}
 
This default behaviour can be overriden by explicitly specifying a boolean operator for the query.  Valid operators are as follows:
* and -	At least one occurrence of every value must be present in one of the specified fields to produce a hit. (This is the default behaviour if no operation is specified).
* or - An occurrence of any of the specified values in any of the specified fields will produce a hit.
 
Restating the previous example with the operator explicitly specified illustrates the difference between the two operations:

_Example - Multi field term query with AND operator explicitly set:_

    {"must": {"match": {"field": "first_name last_name", "value": "Smith Will", "operator": "and"}}}
  
Produces a match for document {"first_name": "Will", "last_name": "Smith"} but not {"first_name": "Will", "last_name": "Shakespeare"} -- Exactly as in our previous example since this is the default behaviour.
  
  
_Example - Multi field term query with OR operator explicitly set:_

    {"must": {"match": {"field": "first_name last_name", "value": "Smith Will", "operator": "or"}}}
  
Produces a match for both documents {"first_name": "Will", "last_name": "Smith"} and {"first_name": "Will", "last_name": "Shakespeare"}

**Parsed Query**

Parsed queries apply a query parser to the supplied query string in order to determine the exact query to apply to the specified field.
The query string is parsed into a series of terms and operators, as described below:

_Terms_

Terms may be any of the following:
* single words
* exact phrases, as denoted by enclosing the phrase in open and close quotations.  Example: "this is my exact phrase"
* regular expressions, as denoted by wrapping the expressing in forward slash ( / ) character.  Example: /joh?n(ath[oa]n)/

Note that a series of terms with no explicit operators will be interpreted as a set of optional values.  For example:

    quick brown fox  would match fields which contain quick OR brown OR fox.

_Operators_

Operators provide for more complexity in terms of the behaviour described by the query string.  The supported operators are as follows:

| Operator | Description                                                              | Example        |
| -------- | ------------------------------------------------------------------------ | -------------- |
| +	       | The term to the right of the operator must be present.	                  | +quick         |
| -	       | The term to the right of the operator must not be present                | -brown         |
| AND	   | Both the terms to the left and right of the operator must be present     |	brown AND fox  |
| OR	   | Either the term to the left or right of the operator must be present     | quick OR brown |
| NOT	   | The term to the right of the operator must not be present (similar to -) | NOT fox        |
 
The following pseudo-json illustrates the structure of a parsed query:

    "parsed-query": {
        "field": "fieldname",      // If this field is not present, then apply the query to ALL fields
        "query-string": "string"
    }


**Range Query**

_Range_ queries match fields whose term value falls within the specified numeric or date range.

For fields containing date types, the default format for that field (as provided in the document schema when the index was created) will be used, unless the client overrides that with their own format string.

A _Range_ query includes one or a combination of operations representing the upper and lower bounds of the range.  The following describes the supported bounds operations:

| Operation | Description            |
| --------- | ---------------------- |
| gt	    | Greater than           |
| gte	    | Greater than or equals |
| lt	    | Less than              |
| lte	    | Less than or equals    |
 
The following pseudo-json describes the structure of a ranged query.

    "range": {
        "field": "fieldname",
        "<<operation>>": numeric-or-date-value,     // where <<operation>> is one of the operations from the table above.
              .
              .
        "<<operation>>": numeric-or-date-value,     // where <<operation>> is one of the operations from the table above.
        "format": "format-string"                   // For date ranges, this allows the client to override the format defined 
                                                    // for the field in the document schema.
        "time-zone": "+1:00"                        // For date ranges, this allows the client to specify a time zone parameter 
                                                    // to be applied to the lower and upper bounds of the query.
    }

### The Aggregations Stanza

**Group By Aggregation**

_Group By_ aggregations create buckets based on the values in a specific field.  These are expressed in the following manner:

_Example - Group by last name, excluding any buckets with less than 2 entries._

    {
        "name": "GroupByLastName",
        "aggregation" : {
            "group-by": {
                "field": "LastName",
                "min-threshold": 2
            }
        }
    }


**Date Range Aggregation**

_Date Range_ aggregations produce counts where date type fields fall in or out of a particular range.

_Example - Return counts of the number of people born before Jan 1, 1972 and the number of people born after Jan 1, 1972_

    {
        "name": "AggregateByBirthdate",
        "aggregation": {
            "date-range": {
                "field": "BirthDate",
                "from": "01-01-1972 00:00:00",
                "to": "01-01-1972 00:00:00"
            }
        }
    }

### Putting It All Together

The following examples illustrate how to construct a complete search statement, putting all of the previous building blocks together, starting with the simplest possible search statement, and building toward more complicated request that combine, filters, scored queries, and aggregations.

_Example - Simple search statement with no filtering or aggregations_

Search Statement:

    {
        "queries": [
            {"must": {"match": {"field": "LastName", "value": "Smith"}}}   
        ]
    }

Response Body:

    {
        "searchResult": {
            "totalHits": 3,
            "hits": [
                {
            	     "score": 0.8,
            	     "document": {
            	         "url": "/indexes/people/documents/8",
            	         "etag": "3",
            	         "content": {
            	             "FirstName": "Will",
            	             "LastName": "Smith"
            	         }
            	     }
            	 },
            	 {
            	     "score": 0.8,
            	     "document": {
            	         "url": "/indexes/people/documents/2",
            	         "etag": "1",
            	         "content": {
            	             "FirstName": "Bob",
            	             "LastName": "Smith"
            	         }
            	     }
            	 },
            	 {
            	     "score": 0.8,
            	     "document": {
            	         "url": "/indexes/people/documents/10",
            	         "etag": "7",
            	         "content": {
            	             "FirstName": "Alan",
            	             "LastName": "Smith"
            	         }
            	     }
            	 }
            ]
        }
    }

_Example - Simple search statement with multiple term queries, with no filtering or aggregations_

Search Statement:

    {
        "queries": [
            {"must": {"match": {"field": "LastName", "value": "Smith"}}},
            {"may": {"match": {"field": "FirstName", "value", "Bob"}}},
            {"must": {"not-match": {"field": "FirstName", "value", "Alan"}}}
        ]
    }

Response Body:

    {
        "searchResult": {
            "totalHits": 2,
            "hits": [
                {
            	     "score": 0.8,
            	     "document": {
            	         "url": "/indexes/people/documents/8",
            	         "etag": "3",
            	         "content": {
            	             "FirstName": "Bob",
            	             "LastName": "Smith"
            	         }
            	     }
            	 },
            	 {
            	     "score": 0.5,
            	     "document": {
            	         "url": "/indexes/people/documents/2",
            	         "etag": "1",
            	         "content": {
            	             "FirstName": "Will",
            	             "LastName": "Smith"
            	         }
            	     }
            	 }
            ]
        }
    }
    
_Example - Simple search statement with a filter stanza_

Search Statement:

    {
        "filter": {
            "all": {
                { "must": {"not-match": {"field": "FirstName", "value", "Bob"}}}
            }
        },
        
        "queries": [
            {"must": {"match": {"field": "LastName", "value": "Smith"}}},
        ]
    }

Response Body:

    {
        "searchResult": {
            "totalHits": 2,
            "hits": [
                {
            	     "score": 0.8,
            	     "document": {
            	         "url": "/indexes/people/documents/8",
            	         "etag": "3",
            	         "content": {
            	             "FirstName": "Will",
            	             "LastName": "Smith"
            	         }
            	     }
            	 },
            	 {
            	     "score": 0.8,
            	     "document": {
            	         "url": "/indexes/people/documents/10",
            	         "etag": "7",
            	         "content": {
            	             "FirstName": "Alan",
            	             "LastName": "Smith"
            	         }
            	     }
            	 }
            ]
        }
    }

_Example - Simple search statement with filter and aggregation stanzas_

Assuming the following document set:

    {"FirstName": "Will", "LastName": "Smith", "BirthDate": "1968-09-25T00:00:00", "Profession": "Actor"},
    {"FirstName": "Jaden, "LastName": "Smith", "BirthDate": "1998-06-08T00:00:00", "Profession": "Actor"},
    {"FirstName": "Alan, "LastName": "Smith", "BirthDate": "1956-05-21T00:00:00", "Profession": "Artist"},
    {"FirstName": "Wilson", "LastName": "Fisk", "BirthDate": "1962-02-17T00:00:00", "Profession": "Crime Lord"},
    {"FirstName": "Bob", "LastName": "Smith", "BirthDate": "1972-11-05T00:00:00", "Profession": "Plumber"},
    {"FirstName": "Jane", "LastName": "Smith", "BirthDate": "1992-10-15T00:00:00", "Profession": "Accountant"},
    {"FirstName": "John", "LastName": "Doe", "BirthDate": "1981-10-15T00:00:00", "Profession": "Janitor"}
    
Filter out all people born before Jan 1, 1960, then query the remaining set for people who's last name is 'Smith', with preference to plumbers, and finally count the number of each profession in the resulting set:

Search Statement:

    {
        "filter": {
            "all": {
                { "must": {"range": {"field": "BirthDate", "gte", "1960-01-01T00:00:00"}}}
            }
        },
        
        "queries": [
            {"must": {"match": {"field": "LastName", "value": "Smith"}}},
            {"may": {"match": {"field": "Profession", "value": "Plumber"}}}
        ],
        
        "aggregations": [
        	{
        		"name": "by_profession",
        		"aggregation": {
        		    "group-by": {
        		        "field": "Profession"
        		    }
        		}
           } 
        ] 
    }

Response Body:

    {
        "searchResult": {
            "totalHits": 4,
            "hits": [
                {
            	     "score": 0.8,
            	     "document": {
            	         "url": "/indexes/people/documents/8",
            	         "etag": "3",
            	         "content": {
            	             "FirstName": "Bob",
            	             "LastName": "Smith",
            	             "Profession": "Plumber"
            	             "BirthDate": "1972-11-05T00:00:00"
            	         }
            	     }
            	 },
            	 {
            	     "score": 0.5,
            	     "document": {
            	         "url": "/indexes/people/documents/10",
            	         "etag": "7",
            	         "content": {
            	             "FirstName": "Will",
            	             "LastName": "Smith",
            	             "Profession": "Actor",
            	             "BirthDate": "1968-09-25T00:00:00"
            	         }
            	     }
            	 },
            	 {
            	     "score": 0.5,
            	     "document": {
            	         "url": "/indexes/people/documents/10",
            	         "etag": "7",
            	         "content": {
            	             "FirstName": "Jaden",
            	             "LastName": "Smith",
            	             "Profession": "Actor",
            	             "BirthDate": "1998-06-08T00:00:00"
            	         }
            	     }
            	 },
            	 {
            	     "score": 0.5,
            	     "document": {
            	         "url": "/indexes/people/documents/10",
            	         "etag": "7",
            	         "content": {
            	             "FirstName": "Jane",
            	             "LastName": "Smith",
            	             "Profession": "Accountant",
            	             "BirthDate": "1992-10-15T00:00:00"
            	         }
            	     }
            	 }
            ]
        },
        "aggregationResult": {
            "aggregations": [
                {
                    "name": "by_profession",
                    "buckets": [
                        { "key": "Actor", "count": 2 },
                        { "key": "Plumber", "count": 1 },
                        { "key": "Accountant", "count": 1 }
                    ]
            ]
        }
    }


## API

### Submit a Search Query

---
**URL**

    https://{host}:9509/services/search-data-service/v1/search/indexes/{index}/query/

**Method** 

    POST

**URL Params**

    index - The name of the _Index_ to apply the query against.

**Request Header**

    Accept          = application/json
    X-TransactionId = Unique id set by client (for logging purposes)
    X-FromAppId     = Application identifier (for logging purposes)
    Content-Type    = application/json
    
**Request Payload**

    Search statement expressed in JSON format (see **Syntax**) 

**Success Response**

    Code:      200
    Header(s): None
    Body:      JSON format result set.  
    
**Error Response**

    400 - Bad Request
    403 - Unauthorized
    500 - Internal Error

---
