#!/bin/bash

echo "-------------------------------------------------------------------"
echo "Starting / Restartign Kafka broker ... "
echo "-------------------------------------------------------------------"


# Se positionner dans le répertoire où se trouve ce script (run_kafka.sh)
cd "$(dirname "$0")"

echo "Arrêt des services existants..."
docker-compose down -v

echo "Nettoyage des stores..."
rm -rf /c/tmp/kafka-streams/ens-notification-consumer-demo

echo "Démarrer les services Docker Compose"
docker-compose up -d

#echo "Attendre que le broker Kafka soit accessible ..."
#docker-compose exec broker1 sh -c "until nc -z 127.0.0.1 9092; do sleep 1; done"

echo "akhq is running : http://localhost:8083"