# ubirch-storage-service
ubirch generic storage service 

## Scala Dependencies

    resolvers += Resolver.sonatypeRepo("snapshots")
    libraryDependencies ++= Seq(
      "com.ubirch.backend.storage" %% "model" % "0.0.1-SNAPSHOT"
      "com.ubirch.backend.storage" %% "share" % "0.0.1-SNAPSHOT"
      "com.ubirch.backend.storage" %% "core" % "0.0.1-SNAPSHOT"
      "com.ubirch.backend.storage" %% "server" % "0.0.1-SNAPSHOT"
      "com.ubirch.backend.storage" %% "client" % "0.0.1-SNAPSHOT"
    )

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