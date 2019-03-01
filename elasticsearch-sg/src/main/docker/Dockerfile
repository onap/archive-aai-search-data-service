# https://github.com/elastic/elasticsearch-docker
FROM docker.elastic.co/elasticsearch/elasticsearch:6.1.2

USER elasticsearch

# Search Guard plugin
RUN elasticsearch-plugin install --batch com.floragunn:search-guard-6:6.1.2-22.3 \
	&& chmod +x plugins/search-guard-6/tools/*.sh

ENTRYPOINT ["/usr/share/elasticsearch/bin/run.sh"]