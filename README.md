# ubirch-storage-service
ubirch generic storage service 

## Scala Dependencies

### `client`

    resolvers ++= Seq(
	  Resolver.sonatypeRepo("snapshots"),
	  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
	)
    libraryDependencies ++= Seq(
      "com.ubirch.backend.storage" %% "client" % "0.1.0-SNAPSHOT"
    )

### `core`

    resolvers ++= Seq(
	  Resolver.sonatypeRepo("snapshots"),
	  "RoundEights" at "http://maven.spikemark.net/roundeights", // Hasher
	  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
	)
    libraryDependencies ++= Seq(
      "com.ubirch.backend.storage" %% "core" % "0.1.0-SNAPSHOT"
    )

### `model`

    resolvers ++= Seq(
	  Resolver.sonatypeRepo("snapshots")
	)
    libraryDependencies ++= Seq(
      "com.ubirch.backend.storage" %% "model" % "0.1.0-SNAPSHOT"
    )

### `share`

    resolvers ++= Seq(
	  Resolver.sonatypeRepo("snapshots"),
	  "RoundEights" at "http://maven.spikemark.net/roundeights" // Hasher
	)
    libraryDependencies ++= Seq(
      "com.ubirch.backend.storage" %% "share" % "0.1.0-SNAPSHOT"
    )

### `test-util`

    resolvers ++= Seq(
	  Resolver.sonatypeRepo("snapshots"),
	  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
	)
    libraryDependencies ++= Seq(
      "com.ubirch.backend.storage" %% "test-util" % "0.1.0-SNAPSHOT"
    )

### `ubirch-share`

    resolvers ++= Seq(
	  Resolver.sonatypeRepo("snapshots"),
	  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
	)
    libraryDependencies ++= Seq(
      "com.ubirch.backend.storage" %% "ubirch-share" % "0.1.0-SNAPSHOT"
    )

### `server`

    resolvers ++= Seq(
	  Resolver.sonatypeRepo("snapshots")
	)
    libraryDependencies ++= Seq(
      "com.ubirch.backend.storage" %% "server" % "0.1.0-SNAPSHOT"
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
    unminedHashesLimit = 10000
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

## create docker image
```
./sbt server/docker
```