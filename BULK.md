# Bulk Operations

## Overview

Bulk operations allow the client to bundle a number of actions into a single REST request.

It is important to note that the individual operations bundled into a bulk request are considered by the Search Service to be completely independent operations.  This has a few consequences:

* No guarantees are made with respect to the order in which the individual operations will be processed by the document store.

* There is no implied transactionality between the operations.  The operations may succeed or fail independently of one another, and it is entirely possible to get back a result set indicating a mix of success and failure results for the individual operations.

## Syntax

The request payload of a bulk operation must be structured in the following manner (we will flesh out this pseudo-json with a concrete example further down):

    [
        { <operation> : { <meta-data>, <document>  } },
                            .
                            .
        { <operation> : { <meta-data>, <document>  } }
    ]
    
**Operation**
The following table describes the operations which are supported as part of a _Bulk_ request:

| Operation | Behaviour                                      | Expected Meta Data     | Expected Payload  |
| --------- | ---------------------------------------------- | ---------------------- | ----------------- |
| create    | Insert a new document into the document store. | document url           | document contents |   
| update    | Update an existing document.                   | document url, etag     | document contents |
| delete    | Remove a document from the document store.     | document url, etag     | none              |  
           
**Meta-Data**
Depending on the operation being requested, certain additional meta-data is required for the _Search Date Service_ to be able to carry out the operation.  These are described in the _operations_ table above.

**Document**
For those operations which involve creating or updating a _Document_, the contents of the document must be provided.

_Example - Simple Bulk Request including all supported operations:_

Request Payload:

	[
	  {
	    "create": {
	      "metaData": {
	        "url": "/indexes/my-index/documents/"
	      },
	      "document": {
	        "field1": "value1",
	        "field2": "value2"
	      }
	    }
	  },
	  {
	    "update": {
	      "metaData": {
	        "url": "/indexes/my-other-index/documents/3",
	        "etag": "5"
	      },
	      "document": {
	        "field1": "some-value"
	      }
	    }
	  },
	  {
	    "delete": {
	      "metaData": {
	        "url": "/indexes/my-index/documents/7"
	      }
	    }
	  }
	]

Response Payload:

	{ 
	    "total_operations": 3, 
	    "total_success": 2, 
	    "total_fails": 1, 
	    "results": [
	        {
	            "operation": "create", 
	            "url": "/services/search-data-service/v1/indexes/my-index/documents/1", 
	            "etag": "1", 
	            "status-code": "409", 
	            "status-message": "[default][1]: document already exists"
	        }, 
	        {
	            "operation": "update", 
	            "url": "/services/search-data-service/v1/indexes/my-other-index/documents/3", 
	            "etag": 6, 
	            "status-code": "200", "status-message": "OK"
	        }, 
	        {
	            "operation": "delete", 
	            "url": "/services/search-data-service/v1/indexes/my-index/documents/7", 
	            "status-code": "200", "status-message": "OK"
	        }
	    ]
	}
	
## API

**Submit A Bulk Operation**
---
**URL**

    https://{host}:9509/services/search-data-service/v1/search/bulk/

**Method** 

    POST

**URL Params**

    None

**Request Header**

    Accept          = application/json
    X-TransactionId = Unique id set by client (for logging purposes)
    X-FromAppId     = Application identifier (for logging purposes)
    Content-Type    = application/json
    
**Request Payload**

    Set of bulk operations to be executed (see Syntax Section) 

**Success Response**

    Code:      207 (Multi-Staltus)
    Header(s): None
    Body:      JSON format result set which includes individual status codes for each requested operation.  
    
**Error Response**

    400 - Bad Request
    403 - Unauthorized
    500 - Internal Error

---
