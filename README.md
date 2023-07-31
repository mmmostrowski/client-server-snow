## Quick run

1. Run the following command in the terminal:
```bash
docker compose up 
```
2. Visit http://localhost:3000/


## Develop in IDE

1. Start the Java Proxy server by running `bootRun` Gradle action in IDE,
2. Start the Web Client by executing the following command in the terminal:
```bash
./dev docker compose up --build reactjs-client 
```
3. Launch the PHP CLI by running the following command in the terminal and then follow the further instructions:
```bash
./dev docker-compose run java-proxy bash 
```

Visit http://localhost:8181/


## Develop without IDE

1. Run the following command in the terminal:
```bash
./dev docker compose up --build
```
2. Visit http://localhost:8181/
3. Make changes and repeat from step 1
