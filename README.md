# startship

![demo](demo.gif)

```shell script
$ brew install saket/repo/startship
$
$ cd ~/path/to/your/library/project
$ startship release && say "released"
```

StartShip makes the process of releasing Android libraries _a bit_ easier by automating usage of Sonatype Nexus so that you don't have to constantly refresh your browser after every operation to check if has gone through yet. 

StartShip will find your staged repository, mark it as closed, wait for it to be actually closed, promote it to release, and wait for it to be synced to maven central. It also tries to be helpful by making sure you don't release an incorrect artifact by comparing maven coordinates and versions.

### License

```
Copyright 2020 Saket Narayan.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
