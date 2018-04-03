# Documents

## Overview
_Documents_ represent the _things_ that we want to store in the _Search Data Service_ and are themselves, basically, a set of fields containing the data that we want to persist.


## Syntax
_Document_ contents are specified as a simple JSON object.  The structure of the _Document_ JSON should match the schema provided to the _Search Data Service_ when the _Index_ was created.

For a discussion of how to specify the _Document Structure_, refer to [Index API](./INDEXES.md). 

**Example - Simple Document **

    {
       "FirstName": "Bob",
       "LastName": "Smith",
       "Age": 43
    }

**Example - Document With Nested Fields **

    {
        "FirstName": "Sherlock",
        "LastName": "Holmes",
        "Address": {
        	"Street": "222B Baker",
        	"City": "London",
        	"Country": "England"
        }
    }
    
## API

### Create Document
Persists a _Document_ in an _Index_ in the _Search Data Service_.

Note, that there are two variants of document creation: with and without supplying an id to associate with the document.

**Create Document (No Id Specified)**

If no _Id_ is provided by the client, then a unique identifier will be generated by the _Search Data Service_.

---
**URL**

    https://{host}:9509/services/search-data-service/v1/search/indexes/{index}/documents/

**Method** 

    POST

**URL Params**

    index - The name of the _Index_ to persist the _Document_ in.

**Request Header**

    X-Create-Index  = true = Allow index to be implicitly created if it does not already exist in the document store.
    
**Request Payload**

    Document contents expressed as a JSON object. (see **Syntax**) 

**Success Response**

    Code:      201
    Header(s): ETag = ETag for the document instance that was just created.
    Body:      URL identifying the document that was just created.  
               Example:
                     {"url": "indexes/myindex/documents/AVgGq2jz4aZeqcwCmlQY"}
    
**Error Response**

    400 - Bad Request
    403 - Unauthorized
    500 - Internal Error

---

**Create Document (Client Supplied Id)**

If the client supplies an identifier for its document then that is what will be used for the document id.

_NOTE: If a document id is supplied then it is the responsibility of the client to ensure uniqueness._  

---
**URL**

    https://{host}:9509/services/search-data-service/v1/search/indexes/{index}/documents/{id}

**Method** 

    PUT

**URL Params**

    index - The name of the _Index_ to persist the Document in.
    id    - The identifier to associate with this Document.

**Request Header**

    X-Create-Index  = true = Allow index to be implicitly created if it does not already exist in the document store.
    
**Request Payload**

    Document contents expressed as a JSON object. (see **Syntax**) 

**Success Response**

    Code:      201
    Header(s): ETag = ETag for the document instance that was just created.
    Body:      URL identifying the document that was just created.  
               Example:
                     {"url": "indexes/myindex/documents/AVgGq2jz4aZeqcwCmlQY"}
    
**Error Response**

    400 - Bad Request
    403 - Unauthorized
    409 - Conflict -- Will occur if a document with that Id already exists
    500 - Internal Error

---


### Retrieve Document
Perform a straight look up of a particular _Document_ by specifying its unique identifier.

---
**URL**

    https://{host}:9509/services/search-data-service/v1/search/indexes/{index}/documents/{id}

**Method** 

    GET

**URL Params**

    index - The name of the _Index_ to persist the Document in.
    id    - The identifier to associate with this Document.

**Request Payload**

    NONE 

**Success Response**

    Code:      200
    Header(s): ETag = ETag indicating the current version of the document.
    Body:      Document contents expressed as a JSON object.  
               Example:
                     {
                         "url": "indexes/myindex/documents/AVgGq2jz4aZeqcwCmlQY"
                         "content": {
                             "firstName": "Bob",
                             "lastName": "Smith",
                             "age": 43
                         }    
                     }
    
**Error Response**

    400 - Bad Request
    404 - Not Found
    500 - Internal Error

---

### Update Document
Replace the contents of a document which already exists in the _Search Data Service_.

**Optimistic Locking On Update**

The _Search Data Service_ employs an optimistic locking mechanism on _Document_ updates which works as follows:

The ETag response header field is set in the response for each document create or update to indicate the most recent version of the document in the document store.

When performing a _Document_ update, this value must be supplied in the _If-Match_ field in the request header.  Failure to supply this value, or failure to provide a value which matches the version in the document store will result in a request failure with a 412 (Precondition Failed) error.

---
**URL**

    https://{host}:9509/services/search-data-service/v1/search/indexes/{index}/documents/{id}

**Method** 

    PUT

**URL Params**

    index - The name of the _Index_ to persist the Document in.
    id    - The identifier to associate with this Document.

**Request Header**

    Accept          = application/json
    X-TransactionId = Unique id set by client (for logging purposes)
    X-FromAppId     = Application identifier (for logging purposes)
    X-Create-Index  = true = Allow index to be implicitly created if it does not already exist in the document store.
    Content-Type    = application/json   
    If-Match        = The ETag value for the document to be updated.

**Request Payload**

    Document contents expressed as a JSON object. (see Syntax Section) 

**Success Response**

    Code:      200
    Header(s): ETag = ETag indicating the current version of the document.
    Body:      URL identifying the document that was just created.  
               Example:
                     {"url": "indexes/myindex/documents/AVgGq2jz4aZeqcwCmlQY"}
    
**Error Response**

    400 - Bad Request
    403 - Unauthorized
    404 - Not Found
    412 - Precondition Failed -- Supplied ETag does not match the version in the document store.
    500 - Internal Error

---

### Delete Document
Remove an existing _Index_ from the _Search Data Service_.  
Note that this results in the removal of all _Documents_ that are stored in the _Index_ at the time that the DELETE operation occurs.

**Optimistic Locking On Update**

As for _Document_ updates, the ETag value must be supplied in the _If-Match_ field in the request header.  

Failure to supply this value, or failure to provide a value which matches the version in the document store will result in a request failure with a 412 (Precondition Failed) error.

---
**URL**

    https://{host}:9509/services/search-data-service/v1/search/indexes/{index}/documents/{id}

**Method** 

    DELETE

**URL Params**

    index - The name of the _Index_ to persist the Document in.
    id    - The identifier to associate with this Document.

**Request Header**

    Accept          = application/json
    X-TransactionId = Unique id set by client (for logging purposes)
    X-FromAppId     = Application identifier (for logging purposes)
    Content-Type    = application/json
    If-Match        = The ETag value for the document to be deleted.
    
**Request Payload**

    NONE 

**Success Response**

    Code:      200
    Header(s): None.
    Body:      None.  
    
**Error Response**

    400 - Bad Request
    403 - Unauthorized
    404 - Not Found
    412 - Precondition Failed -- Supplied ETag does not match the version in the document store.
    500 - Internal Error

---