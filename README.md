# Search Engine Micro Service

The _Search Engine_ micro service exposes APIs via REST which allow clients to interact with the search database back end without requiring direct knowledge of or interaction with the underlying technology.
 
## High Level Concepts
This section establishes some of the terminology and concepts that relate to interacting with the _Search Engine_ service.
A much more detailed examination of these concepts can be found on the  [Search Engine Design Share](http://d2athenaconf:8090/confluence/display/AAI/AAI-4633%3A+Search+DB+Abstraction%3A+Expose+REST+Interface) Confluence page.

### Documents
_Documents_ are the _things_ that we want to put into our document store.  At its most basic, a _document_ is a collection of _fields_ which contain the data that we want to be able to store and query.

_Fields_ are defined as having a name, a type, and optional parameters indicating whether or not the field is intended to be searchable, and if so, how it should be indexed.

### Indexes
An _index_ is essentially a collection of _documents_.  It is the top-level container into which we will store our documents.  A single data store may have multiple _indexes_ for the purposes of segregating different types of data (most queries are performed across all documents within  *single* instance).

---
## Getting Started

### Building The Micro Service

After checking out the project, execute the following Maven command from the project's top level directory:

    > mvn clean install
    
### Running The Micro Service Locally
To run the microservice in your local environment, execute the following Maven command from the project's top level directory:

    > mvn -P runAjsc

### Running The Micro Service Within An Eclipse Environment
It is often extremely useful to be able to run a micro service from within Eclipse in order to set breakpoints and perform general debugging activities.

For a good reference on how to launch any of the D2 micro services from within an Eclipse environment, refer to the following Confluence page: [Running An AJSC Container Within Eclipse](http://d2athenaconf:8090/confluence/pages/viewpage.action?pageId=1840887#DevelopingMicroserviceswithAT&T-RunninganAJSCContainerwithinEclipse)

---

## Public Interfaces

### Echo Service
The _Search Database Abstraction_ micro service supports the standard echo service to allow it to be 'pinged' to verify that the service is up and responding.

The echo service is reachable via the following REST end point:

    http://{host}:9509/services/search-data-service/v1/jaxrsExample/jaxrs-services/echo/{input}

### Indexes
The _Search Engine_ service supports simple creation and deletion of document indexes via the following REST API calls:

##### Create Index
    Method         : POST
    URL            : https://<host>:9509/services/search-data-service/v1/search/indexes/<index>/
    URL Params     : index - The name of the index to be created.
    Request Payload:
        A document structure expressed as json.
        
    Response Payload:
        {"url": "< resource location of the index >"

##### Delete Index
    Method         : DELETE
    URL            : http://<host>:9509/services/search-data-service/v1/search/indexes/<index>/
    URL Params     : index - The name of the index to be deleted.
    Request Payload:
        None    
        
   
### Documents
 
##### Create Document Without Specifying a Document Identifier
Documents can be created via a POST request with no document identifier specified.  In this case the document store will generate an identifier to associate with the document.

    Method         : POST
    URL            : https://<host>:9509/services/search-data-service/v1/search/indexes/<index>/documents/
    URL Params     : index       - The name of the index to create the document in.
    Request Payload:
        Document contents expressed as a JSON object containing key/value pairs.
        
    Response Payload:
        { "etag": "string", "url": "string" }
        
##### Create or Update Document With a Specified Document Identifier
Documents can also be created via a PUT request which includes an identifier to associate with the document.  The put endpoint is actually used for both creates and updates, where this is distinguished as follows:
* If the request header DOES NOT include a value in the If-Match field, then the request is assumed to be a document create.
* If the request header DOES contain a value in the If-Match field, then the request is assumed to be a document update.

    Method         : PUT
    URL            : https://<host>:9509/services/search-data-service/v1/search/indexes/<index>/documents<document id>
    URL Params     : index       - The name of the index to create or update the document in.
                     document id - The identifier of the document to be created or updated.
    Request Payload:
        Document contents expressed as a JSON object containing key/value pairs.
        
    Response Payload:
        { "etag": "string", "url": "string"}
        
##### Delete a Document

    Method:        : DELETE
    URL            : https://<host>:9509/services/search-data-service/v1/search/indexes/<index>/documents<document id>
    URL Params     : index       - The name of the index to remove the document from.
                     document id - the identifier of the document to be deleted.
    Request Payload:
        None.
        
##### Retrieve a Document

    Method:        : GET
    URL            : https://<host>:9509/services/search-data-service/v1/search/indexes/<index>/documents<document id>
    URL Params     : index       - The name of the index to retrieve the document from.
                     document id - the identifier of the document to be retrieved.
    Request Payload:
        None.
        

### Searching the Document Store
Search statements are passed to the _Search Data Service_ as a JSON object which is structured as follows:

_Filters_
* A "filter" stanza defines a set of queries to be run in _non-scoring-mode_ to reduce the document set to a smaller subset to be searched.
* The filter stanza is optional - omitting it implies that the query is _unfiltered_.
* This stanza is represented as a JSON object with the following structure:

    "filter": {
                "all": [ { query }, { query },....{ query }],
                "any": [ { query }, { query },....{ query }]
    },

Where: 
* the _all_ list defines a set of queryies such that ALL queries in the list must be satisfied for the document to pass the filter.
* the _any_ list defines a set of queryies such that ANY single query in the list must be satisfied for the document to pass the filter. 

_Queries_
The following types of query statements are supported by the _Search Data Service_:

_Term Query_:

A term query attempts to match the literal value of a field, with no advanced parsing or analysis of the query string.  This type of query is most appropriate for structured data like numbers, dates and enums, rather than full text fields.

     // Find documents where the specified field contains the supplied value
    "match": {
        "field": "value"
    }
  
    // Find documents where the specified field DOES NOT contain the supplied value
    "not-match": {
        "field": "value"
    }
    
_Parsed Query_:

Parsed queries apply a query parser to the supplied query string in order to determine the exact query to apply to the specified field.
The query string is parsed into a series of terms and operators, as described below:

Terms may be any of the following:
* single words
* exact phrases, as denoted by enclosing the phrase in open and close quotations.  Example: "this is my exact phrase"
* regular expressions, as denoted by wrapping the expressing in forward slash ( / ) character.  Example: /joh?n(ath[oa]n)/

The supported operators are as follows:
* AND - Both terms to the left or right of the operator MUST be present
* OR  - Either the term to the left or right of the operator MUST be present
* NOT - The term to the right of the operator MUST NOT be present.

    "parsed-query": {
        "field": "fieldname",
        "query-string": "string"
    }
    
_Range Query_:

 Range queries match fields whose term value falls within the specified numeric or date range.
 Supported bounds operators include:
 * gt  - Greater than
 * gte - Greater than or equal to
 * lt  - Less than
 * lte - Less than or equal to
 
     "range": {
        "field": "fieldname",
        "operator": "value",
        "operator": "value"
     }
        
##### Examples
The following snippet illustrates a search statement describing a filtered query which uses examples of all of the supported query types:

    {
        "filter": {
            "all": [{"range": {"field": "timestamp", "lte": "2016-12-01T00:00:00.558+03:00"}}],
            "any": [ ]
        },
        
        "queries": [
            {"match": {"field": "name", "value": "Bob"}},
            {"parsed-query": {"field": "street-name", "query-string": "Main OR First"}},
            {"range": {"field": "street-number", "gt": 10, "lt": 50}}
        ]
    }

##### REST Endpoint

    Method:        : POST
    URL            : https://<host>:9509/services/search-data-service/v1/search/indexes/<index>/query
    URL Params     : index       - The name of the index to apply the query to.

    Request Payload:
        {
            "filter": {
                "all": [ { query }, { query },....{ query }],
                "any": [ { query }, { query },....{ query }]
            },
            
            "queries": [
                { query },
                    .
                    .
                { query }
            ]
        }

### Bulk Operations
Bulk operations allow the client to bundle a number of actions into a single REST request.
It is important to note that individual operations bundled into a bulk request are considered by the _Search Service_ to be completely independent operations.  This has a few important consequences:
* No guarantees are made with respect to the order in which the individual operations will be processed by the document store.
* There is no implied transactionality between the operations.  Individual operations my succeed or fail independently of one another, and it is entirely possible for the client to receive back a result set indicating a mix of success and failure results for the individual operations.

##### Submit Bulk Request
    Method        : POST
    URL           : http://<host>:9509/services/search-data-service/v1/search/bulk/
    URL Params    : NONE
    Request Payload:
        A json structure containing all of the bundled actions to be performed.
        It must correspond to the following format:
            [
                { "operation": {{<metaData>}, {<document>},
                { "operation": {{<metaData>}, {<document>},
                            .
                            .
                { "operation": {{<metaData>}, {<document>},
            ]
            
        Where,
            operation - Is one of:  "create", "update", or "delete"
            
            metaData  - A structure containing meta-data associated with the individual operation to be performed.  Valid fields include:
                "url"   - The resource identifier of the document to be operated on.
                "etag" - Identifies the version of the document to be acted on.  Required for "update" and "delete" operations.
                
            document - The document contents for "create" and "update" operations.
            
        Example Payload:
        [
            {"create": {"metaData": {"url": "/services/search-data-service/v1/indexes/the-index/documents/1"}, "document": {"f1": "v1", "f2": "v2"}}},
            {"create": {"metaData": {"url": "/services/search-data-service/indexes/the-index/documents/2"}, "document": {"f1": "v1", "f2": "v2"}}},
            {"update": {"metaData": {"url": "/services/search-data-service/v1/search/indexes/the-index/documents/8", "etag": "1"}, "document": {"f1": "v1a", "f2": "v2a"}}},
            {"delete": {"metaData": {"url": "/services/search-data-service/v1/search/indexes/the-index/documents/99", "etag": "3"}}}
        ]
        
    Response Payload:
        The response body will contain an aggregation of the collective results as well as separate status codes for each of the operations in the request.
        Example:
        { 
            "total_operations": 4, 
            "total_success": 1, 
            "total_fails": 3, 
    			"results": [
        			{"operation": "create", "url": "/services/search-data-service/v1/search/indexes/the-index/documents/1", "etag": "1", "status-code": "201", "status-message": "OK"}, 
        			{"operation": "create", "url": "/services/search-data-service/v1/search/indexes/the-index/documents/2", "etag": "1", "status-code": "201", "status-message": "OK"}, 
        			{"operation": "update", "url": "/services/search-data-service/v1/search/indexes/the-index/documents/8", "etag": "2", "status-code": "200", "status-message": "OK"}, 
        			{"operation": "delete", "url": "/services/search-data-service/v1/search/indexes/the-index/documents/2", "status-code": "200", "status-message": "OK"}
    ]
}