curl -X PUT \
  https://search-es-web-elastic-1exwx2kpv9s4v-uhr24uozaed2xhu7dteatg6pg4.us-west-2.es.amazonaws.com/_template/sessions \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: e6f47f87-9161-4f9a-8bae-5e0858aba180' \
  -H 'cache-control: no-cache' \
  -d '{
    "index_patterns": [
        "sessions*"
    ],
    "order": -1,
    "settings": {
        "number_of_shards": 10,
        "refresh_interval": "30s",
	"number_of_replicas": "1"
    },
    "mappings": {
            "properties": {
                "timestamp": {
                    "type": "date"
                },
                "an": {
                    "type": "keyword"
                },
                "cdn": {
                    "type": "keyword"
                },
                "cid": {
                    "type": "keyword"
                },
                "ct": {
                    "type": "keyword"
                },
                "id": {
                    "type": "keyword"
                },
                "pt": {
                    "type": "keyword"
                },
                "ptv": {
                    "type": "keyword"
                },
                "vid": {
                    "type": "keyword"
                },
                "sst": {
                    "type": "date"
                },
                "pod": {
                    "type": "keyword"
                }
            }
    }
}'
