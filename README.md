## Quick run ( Linux / MacOs / Windows )

1. To quickly run the application execute the following command in the terminal:
```bash
docker run --name php-java-react-snow-client -it --rm -p3000:3000 mmmostrowski/php-java-react-snow-client
```
Note: press Ctrl+C to stop
2. Open a second new terminal and execute:
```bash
docker run --name php-java-react-snow-proxy -it --rm -p8080:8080 mmmostrowski/php-java-react-snow-proxy snow-server
```
Note: press Ctrl+C to stop
3. Visit http://localhost:3000/ in your web browser




## Run ( Linux / MacOs / Windows )

1. Clone project and execute the following command in the terminal from cloned folder:
```bash
docker compose up 
```
3. Visit http://localhost:3000/ in your web browser


## Develop in an IDE 
### ( Linux / MacOs / Windows WSL )

1. Start the Java Proxy server by running `bootRun` Gradle action in IDE,
2. To run the Web Client, execute the following command in the terminal:
```bash
./dev docker compose up --build reactjs-client 
```
3. Launch the PHP CLI by running the following command in the terminal and then follow the further instructions:
```bash
./dev docker compose run java-proxy bash 
```
4. To reflect Java Proxy changes you need to re-lunch `bootRun` Gradle action.
   Changes made to the Web Client will be automatically reflected in the web browser due to React's hot-reloading feature.
5. You can run tests by executing the `test` Gradle action. 

Visit http://localhost:8181/ to access the application.


## Develop without an IDE 
### ( Linux / MacOs / Windows WSL )

1. To develop without an IDE, run the following command in the terminal:
```bash
./dev docker compose up --build
```
2. Access the application at http://localhost:8181/ in your web browser
3. Make changes and repeat the process from step 1 as needed
4. You can run tests by executing the following command in the terminal:
```bash
./dev docker compose run java-proxy test
```


## Cleanup
1. When you are done, use the following command in the terminal to clean up the environment:
```bash
docker compose down -v
```

<br>
Note: Please make sure you have Docker installed and set up on your system before running the commands above.