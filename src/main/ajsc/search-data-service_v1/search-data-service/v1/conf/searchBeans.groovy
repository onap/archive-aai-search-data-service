import org.onap.aai.sa.rest.SearchServiceApi

beans {
    xmlns cxf: "http://camel.apache.org/schema/cxf"
    xmlns jaxrs: "http://cxf.apache.org/jaxrs"
    xmlns util: "http://www.springframework.org/schema/util"

    searchServiceAPI(SearchServiceApi)

    util.list(id: 'searchServices') {
        ref(bean: 'searchServiceAPI')
    }
}