# Search Data Service Micro Service

## Overview
The _Search Data Service_ acts as an abstraction layer for clients which have a need to interact with data which is most suitably persisted in a searchable document store.  The purpose of imposing an abstraction service between the client and the document store itself is to decouple clients from any direct knowledge of any specific document store technology, allowing the underlying technology to be swapped out without a direct impact to any clients which interact with search data.

Please refer to the following sub-sections for more detailed information:

[High Level Concepts](./CONCEPTS.md) - Discussion of the high level concepts and building blocks of the _Search Data Service_.

[Index API](./INDEXES.md) - Details regarding manipulating document indexes.

[Document API](./DOCUMENTS.md) - Details regarding manipulating documents.

[Search API](./SEARCH.md) - Details regarding querying the data set.

[Bulk API](./BULK.md) - Details regarding submitted bulk operation requests.


## Getting Started

### Building The Micro Service

After cloning the project, execute the following Maven command from the project's top level directory to build the project:

    > mvn clean install

Now, you can build your Docker image:

    > docker build -t openecomp/search-data-service target 
    
### Deploying The Micro Service 

Push the Docker image that you have built to your Docker repository and pull it down to the location that you will be running the search service from.

Note that the current version of the _Search Data Service_ uses _ElasticSearch_ as its document store back end.  You must therefore deploy an instance of ElasticSearch and make it accessible to the _Search Data Service_.

**Create the following directories on the host machine:**

    /logs
    /opt/app/search-data-service/appconfig
    
You will be mounting these as data volumes when you start the Docker container.

**Populate these directories as follows:**

##### Contents of /opt/app/search-data-service/appconfig

The following files must be present in this directory on the host machine:

_analysis-config.json_
Create this file with exactly the contents below:


    [
        {
                "name": "whitespace_analyzer",
                "description": "A standard whitespace analyzer.",
                "behaviours": [
                        "Tokenize the text using white space characters as delimeters.",
                        "Convert all characters to lower case.",
                        "Convert all alphanumeric and symbolic Unicode characters above the first 127 ASCII characters into their ASCII equivalents."
                ],
                "tokenizer": "whitespace",
                "filters": [
                        "lowercase",
                        "asciifolding"
                ]
        },
        {
                "name": "ngram_analyzer",
                "description": "An analyzer which performs ngram filtering on the data stream.",
                "behaviours": [
                        "Tokenize the text using white space characters as delimeters.",
                        "Convert all characters to lower case.",
                        "Convert all alphanumeric and symbolic Unicode characters above the first 127 ASCII characters into their ASCII equivalents.",
                        "Apply ngram filtering using the following values for minimum and maximum size in codepoints of a single n-gram: minimum = 1, maximum = 2."
                ],
                "tokenizer": "whitespace",
                "filters": [
                        "lowercase",
                        "asciifolding",
                        "ngram_filter"
                ]
        }
    ]

_filter-config.json:_

Create this file with exactly the contents below:

    [
        {
                "name": "ngram_filter",
                "description": "Custom NGram Filter.",
                "configuration": " \"type\": \"nGram\", \"min_gram\": 1, \"max_gram\": 50, \"token_chars\": [ \"letter\", \"digit\", \"punctuation\", \"symbol\" ]"
        }
    ]
    
_elastic-search.properties_

This file tells the _Search Data Service_ how to communicate with the ElasticSearch data store which it will use for its back end.
The contents of this file will be determined by your ElasticSearch deployment:

    es-cluster-name=<<name of your ElasticSearch cluster>>
    es-ip-address=<<ip address of your ElasticSearch instance>>
    ex.http-port=9200
    


##### Contents of the /opt/app/search-data-service/app-config/auth Directory

The following files must be present in this directory on the host machine:

_search\_policy.json_

Create a policy file defining the roles and users that will be allowed to access the _Search Data Service_.  This is a JSON format file which will look something like the following example:

    {
        "roles": [
            {
                "name": "admin",
                "functions": [
                    {
                        "name": "search", "methods": [ { "name": "GET" },{ "name": "DELETE" }, { "name": "PUT" }, { "name": "POST" } ]
                    }
                ],
                "users": [
                    {
                        "username": "CN=searchadmin, OU=My Organization Unit, O=, L=Sometown, ST=SomeProvince, C=CA"
                    }    
                ]
            }
        ]
    }

_tomcat\_keystore_

Create a keystore with this name containing whatever CA certificates that you want your instance of the _Search Data Service_ to accept for HTTPS traffic.

**Start the service:**

You can now start the Docker container for the _Search Data Service_, in the following manner:

	docker run -d \
	    -p 9509:9509 \
		-e CONFIG_HOME=/opt/app/search-data-service/config/ \
		-e KEY_STORE_PASSWORD={{obfuscated password}} \
		-e KEY_MANAGER_PASSWORD=OBF:{{obfuscated password}} \
	    -v /logs:/opt/aai/logroot/AAI-SDB \
	    -v /opt/app/search-data-service/appconfig:/opt/app/search-data-service/config \
	    --name search-data-service \
	    {{your docker repo}}/search-data-service
    
Where,

    {{your docker repo}} = The Docker repository you have published your Search Data Service image to.
    {{obfuscated password}} = The password for your key store/key manager after running it through the Jetty obfuscation tool.

 
 
 