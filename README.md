# AkkaToZio

## Repo setup
Something handy to have in your git hooks is the one in `./scripts/githooks/pre-commit`
which validates coding standards when trying to run a `git commit` command.

install pre-commit: https://pre-commit.com/

and launch:
```shell
$ cd scripts/githooks
$ pre-commit install
```

|Technology   | Version
|-------------|---------- |
|Scala        | 2.13 |
|SBT          | 1.7.1 |
|JAVA         | 17 |


### To run the shard-cake example:
- ```bash cd docker; docker-compose up redis```
- ```bash sbt "zio/runMain example.complex.ShardManagerApp"```
- ```bash sbt "zio/runMain example.complex.GuildApp"```
