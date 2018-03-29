/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.sa.searchdbabstraction.logging;

import org.onap.aai.cl.eelf.LogMessageEnum;
import com.att.eelf.i18n.EELFResourceManager;

public enum SearchDbMsgs implements LogMessageEnum {

  /**
   * Arguments:
   * None
   */
  SERVICE_STARTED,

  /**
   * Arguments:
   * {0} = url
   */
  ELASTIC_SEARCH_CONNECTION_ATTEMPT,

  /**
   * Arguments:
   * {0} = url
   */
  ELASTIC_SEARCH_CONNECTION_SUCCESS,

  /**
   * Arguments:
   * {0} = url
   * {1} = failure cause
   */
  ELASTIC_SEARCH_CONNECTION_FAILURE,

  /**
   * Arguments:
   * {0} = Filter configuration file.
   * {1} = Failure cause.
   */
  FILTERS_CONFIG_FAILURE,

  /**
   * Arguments:
   * {0} = Analysys configuration file.
   * {1} = Failure case.
   */
  ANALYSYS_CONFIG_FAILURE,

  /**
   * Arguments:
   * {0} = Index name
   */
  CREATED_INDEX,

  /**
   * Arguments:
   * {0} = Index name
   * {1} = Document type
   */
  CREATE_INDEX_TIME,

  /**
   * Arguments:
   * {0} = Index name
   */
  DELETED_INDEX,

  /**
   * Arguments:
   * {0} = Index name
   */
  DELETE_INDEX_TIME,

  /**
   * Arguments:
   * {0} = Index name
   */
  CHECK_INDEX_TIME,

  /**
   * Arguments:
   * {0} = Index name
   */
  CREATE_DOCUMENT_TIME,

  /**
   * Arguments:
   * {0} = Index name
   * {1} = Document id
   */
  UPDATE_DOCUMENT_TIME,

  /**
   * Arguments:
   * {0} = Index name
   * {1} = Document id
   */
  DELETE_DOCUMENT_TIME,

  /**
   * Arguments:
   * {0} = Index name
   * {1} = Document id
   */
  GET_DOCUMENT_TIME,

  /**
   * Arguments:
   * {0} = Index name
   * {1} = Query string
   */
  QUERY_DOCUMENT_TIME,

  /**
   * Arguments:
   */
  BULK_OPERATIONS_TIME,

  /**
   * Arguments:
   */
  PROCESSED_BULK_OPERATIONS,

  /**
   * Arguments:
   * {0} = Event
   * {1} = Result
   */
  PROCESS_EVENT,

  /**
   * Arguments:
   * {0} = URL.
   */
  PROCESS_INLINE_QUERY,

  /**
   * Arguments
   * {0} - Operation type (GET or POST)
   * {1} - URL.
   */
  PROCESS_PAYLOAD_QUERY,

  /**
   * Arguments:
   * {0} = Index
   * {1} = Error
   */
  INDEX_CREATE_FAILURE,

  /**
   * Arguments:
   * {0} = Index name
   * {1} = Error cause
   */
  INDEX_DELETE_FAILURE,

  /**
   * Arguments:
   * {0} = Failure cause.
   */
  GET_ANALYZERS_FAILURE,

  /**
   * Arguments:
   * {0} = Failure cause.
   */
  BULK_OPERATION_FAILURE,

  /**
   * Arguments:
   * {0} = Method
   * {1} = Exception
   */
  EXCEPTION_DURING_METHOD_CALL,

  /**
   * Received request {0} {1} from {2}.  Sending response: {3}
   *
   * <p>Arguments:
   * {0} = operation
   * {1} = target URL
   * {2} = source
   * {3} = response code
   */
  PROCESS_REST_REQUEST,

  STARTUP_EXCEPTION
  /**
   * Exception encountered during startup of search service: {0}
   *
   * <p>Arguments:
   *    {0} = exception
   */
  ;

  /**
   * Load message bundle (SearchDbMsgs.properties file)
   */
  static {
    EELFResourceManager.loadMessageBundle("logging/SearchDbMsgs");
  }

}
