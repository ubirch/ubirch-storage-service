# ubirch-storage-service
ubirch generic storage service 


## client usage

these settings have to be set:
```
ubirchStorageService {
  interface = "localhost"
  port = 8080
  hash {
    algorithm = sha512
  }
  elasticsearch {
    url = "http://localhost:9200"
    devicemessage {
      index = "ubirch-device-data"
    }
    deviceconfig {
      index = "ubirch-device-configs"
      type = "device-config"
    }
    hashchainstore {
      index = "ubirch-chain-hash"
      type = "chain-hash"
    }
    blockchainstore {
      index = "ubirch-chain-block"
      type = "chain-block"
    }
    genesisblockchainstore {
      index = "ubirch-chain-block"
      type = "chain-genesisblock"
    }
  }
}
```