# 272-group3-zk-kafka-utils
This repo contains the common utilities for accessing zk and kafka

Each developer can run the following command in their distributed-microblog github repo inside `/server/src/main/java/edu/sjsu/<projectName>/server/` folder

``
$ git submodule add https://github.com/harsh-vardhan-ande/272-group3-zk-kafka-utils kafka
$ git submodule add https://github.com/harsh-vardhan-ande/272-group3-zk-kafka-utils zookeeper
``

This will create 2 folders kafka and zookeeper inside `/server` which contain the kafka and zookeeper utilities.

To update your repo with the latest code from this repo, run git pull from inside the folder created above.
