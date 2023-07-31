## Quick run

1. Run in terminal:
```bash
docker compose up 
```
2. Visit http://localhost:3000/


## Develop in IDE

1. To start java proxy server, please start `bootRun` gradle action in IDE,
2. To start web client, please run in terminal:
```bash
./dev docker compose up --build reactjs-client 
```
3. To start php cli, please run in terminal and follow further instructions:
```bash
./dev docker-compose run java-proxy bash 
```

Visit http://localhost:8181/


## Develop without IDE

1. Run in terminal:
```bash
./dev docker compose up --build
```
2. Visit http://localhost:8181/
3. Make changes and repeat from step 1
