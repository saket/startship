# StartShip

```
$ brew install saket/repo/startship
$ cd ~/path/to/your/library/project
$ startship release
```

StartShip makes the process of releasing Android libraries _a bit_ easier by automating usage of SonaType Nexus so that you don't have to constantly refresh your browser to check if your artifact is available on maven central. 

It finds your staged repository, marks it as closed, waits for it to be closed, promotes it to release, and waits for it to be synced to maven central. It also tries to be helpful by making sure you don't release an incorrect artifact by comparing maven coordinates and versions.

