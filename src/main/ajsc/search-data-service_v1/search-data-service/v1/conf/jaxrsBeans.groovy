beans {
    xmlns cxf: "http://camel.apache.org/schema/cxf"
    xmlns jaxrs: "http://cxf.apache.org/jaxrs"
    xmlns util: "http://www.springframework.org/schema/util"

    echoService(org.onap.aai.sa.searchdbabstraction.JaxrsEchoService)
    userService(org.onap.aai.sa.searchdbabstraction.JaxrsUserService)
    searchService(org.onap.aai.sa.searchdbabstraction.service.SearchService)

    util.list(id: 'jaxrsServices') {
        ref(bean: 'echoService')
        ref(bean: 'userService')
    }
}
