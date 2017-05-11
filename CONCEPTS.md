# High Level Concepts

The _Search Data Service_ is built around a set of fundamental concepts and building blocks, which we will describe at a high level here.

We will explore in more detail how to express these concepts when interacting with the _Search Data Service_ in more concrete terms in the section on API syntax.

## Index
An _Index_ can be thought of as a collection of _Documents_, and represents the largest granularity of data grouping in the store.

Multiple _Indexes_ may be defined within the document store in order to segregate data that is different enough that we don't typically want to consider them within the same contexts.

For details regarding how to manipulate document indexes, please refer to the [Index API](./INDEXES.md) page.

## Document
_Documents_ are the things we are putting into our Document Store (go figure!) and represent the smallest independent unit of data with which the document store is concerned.

Documents are composed of _Fields_ which contain the documents' data.

### Document Fields
_Fields_ represent the individual bits of data which make up a document and can be thought of as describing the _structure_ of the document.
_Fields_ have a number of attributes associated with them which instruct the _Search Service_ about how to treat the data which they contain.


| Field           | Description                                                              | 
| :-------------- | :----------------------------------------------------------------------- |
| name            | Identifier for the field                                                 | 
| searchable      | Indicates whether or not this field should be indexed.  Defaults to true |
| search_analyzer | Which analyzer should be used for queries.  Defaults to TBD              |
| index_analyzer  | Which analyzer will be used for indexing                                 |

For details regarding how to manipulate documents, please refer to the [Document API](./DOCUMENTS.md) page.

## Analysis
Field analysis is the process of a taking a _Document_'s fields and breaking them down into tokens that can be used for indexing and querying purposes.  How a document is analyzed has a direct impact on the sort of _Queries_ that we can expect to perform against that data.

### Analyzers
An _Analyzer_ can be envisioned as describing a pipeline of operations that a document is run through in the process of persisting it in the document store.  These can be broken down as follows:

     +--------------------+
     |    DATA STREAM     |
     | (ie: the document) |
     +--------------------+
               |
               V
       \---------------/       
        \  Character  / -------------- Apply character level transformations on the data stream.
         \ Filter(s) /
          \---------/
               |
               V
       +----------------+              
       | SANITIZED DATA |
       |     STREAM     |
       +----------------+
               |
               V
        \-------------/      
         \ Tokenizer / --------------- Break the data stream into tokens.
          \---------/
               |
               V
        +--------------+
        | TOKEN STREAM |
        +--------------+
               |
               V
     \--------------------/      
      \  Token Filter(s) / ----------- Apply transformations at the token level (add, remove, or transform tokens)
       \----------------/
               |
               V
      +-------------------+   
      | TRANSFORMED TOKEN |
      |      STREAM       |
      +-------------------+

**Character Filter(s)**
The input stream may first be passed through one or more _Character Filters_ which perform transformations in order to clean up or normalize the stream contents (for example, stripping out or transforming HTML tags, converting all characters to lower case, etc)

**Tokenizer**
The resulting data stream is then tokenized into individual terms.  The choice of tokenizer at this stage determines the rules under which the input stream is split (for example, break the text into terms whenever a whitespace character is encountered).

**Token Filter(s)**
Each term resulting from the previous stage in the pipeline is then run through any number of _Token Filters_ which may apply various transformations to the terms, such as filtering out certain terms, generating additional terms (for example, adding synonyms for a given term), etc.

The set of tokens resulting from the analysis pipeline operations are ultimately used to create indexes in the document store that can later be used to retrieve the data in a variety of ways.

