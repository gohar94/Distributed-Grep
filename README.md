# CS425 MP
## Yang Liu (liu310) and Gohar Irfan Chaudhry (gic2)

### Instructions/Notes

#### For installing this project on a CS425 VM:
```
sudo bash install.sh
```

#### For building the project from a clean slate:
```
mvn clean install
```

#### For starting Daemon with arguments:
```
mvn exec:java -Dexec.mainClass="edu.illinois.cs.cs425.Daemon" -Dexec.args="<portNumber>"
```
**E.g. usage:**
```
mvn exec:java -Dexec.mainClass="edu.illinois.cs.cs425.Daemon" -Dexec.args="6666"
```
*(Note, this will run another server socket on the port `<portNumber> + 1` for the UserHandler)*

#### For executing grep with arguments:
```
mvn exec:java -Dexec.mainClass="edu.illinois.cs.cs425.Grep" -Dexec.args="-p <pattern> -d <directory> -h <hosts (optional .txt file)> -u <userHandlerPort (optional portNumber)"
mvn exec:java -Dexec.mainClass="edu.illinois.cs.cs425.Grep" -Dexec.args="-p <pattern> -d <directory> -h <hosts (optional space separated IP:portNumber)>"
```
**E.g. usage:**
```
mvn exec:java -Dexec.mainClass="edu.illinois.cs.cs425.Grep" -Dexec.args="-p abc$ -d inputs"
mvn exec:java -Dexec.mainClass="edu.illinois.cs.cs425.Grep" -Dexec.args="-p abc$ -d inputs -h 10.0.0.1:6666 10.0.0.2:6668"
mvn exec:java -Dexec.mainClass="edu.illinois.cs.cs425.Grep" -Dexec.args="-p abc$ -d inputs -h hosts.txt"
mvn exec:java -Dexec.mainClass="edu.illinois.cs.cs425.Grep" -Dexec.args="-p abc$ -d inputs -h hosts.txt -u 6668"
```

#### For running *all* Maven Tests:
```
mvn test
```

#### Overview of the program
The program will output intermediate files containing the results from each of the nodes in a folder by the name of `<timestamp>.out` - in this folder, there will be intermediate files following the naming convention of `Host-<IP>:<port>.txt` and a file called `results.txt` containing all the combined results.