![Coverage](.github/badges/jacoco.svg) ![Branches](.github/badges/branches.svg) ( [Report](https://mmmostrowski.github.io/php-java-react-snow/) )

# Snow Live Stream Toy
This project is a continuation of the [PHP Terminal Snow Toy](https://github.com/mmmostrowski/php-snow) project.<br>

<p align="center">
  <img src="assets/screenshot.png" alt="PHP Snow Live Stream Screenshot"/>
</p>
<br>

## Quick start
To quickly run the application, follow these steps:

1. Open a terminal and execute the following command to start the Client:
```shell
docker run -it --rm -p3000:3000 --pull always mmmostrowski/php-java-react-snow-client
```
Note: Press Ctrl+C to stop the client.

2. Open a second terminal and execute the following command to start the Server:
```shell
docker run -it --rm -p8080:8080 --pull always mmmostrowski/php-java-react-snow-proxy snow-server
```
Note: Press Ctrl+C to stop the proxy server.

3. Once both the client and the proxy server are running, visit http://localhost:3000/ on multiple web browser tabs.



<br>

## How it works ?
The Snow Live Stream Toy works as follows:

1. A single PHP CLI process generates an animation and sends it to a pipe.
2. A Java Proxy Server receives the animation from the pipe and buffers it.
3. Multiple React Webapp Clients live-stream the same animation via Websockets and render it.

<p align="center">
  <img src="assets/diagram.png" alt="Application Diagram"/>
</p>



<br>

## Running the Project

1. Clone the project to local folder.
2. Open a new terminal, navigate to the project directory, and execute:
```shell
docker compose up
```
3. To access the application, visit http://localhost:3000/ in your web browser.
4. When you are done, use the following command in the terminal to clean up the environment:
```bash
docker compose down -v
```


<br>

## Develop in an IDE
Follow these steps to develop using an IDE:

1. Start the Java Proxy server by running `bootRun` Gradle action in IDE.
2. To run the Web Client, execute the following command in the terminal:
```shell
./dev docker compose up --build reactjs-client 
```
3. To reflect Java Proxy changes, relaunch the `bootRun` Gradle action. <br>
   Changes made to the Web Client will be automatically reflected in the web browser due to React's hot-reloading feature.
   PHP changes will be automatically updated during each animation start. <br>
4. You can launch the PHP CLI by running the following command in the terminal:
```shell
./dev docker compose run java-proxy bash 
```
5. Run tests by executing the `test` Gradle action.
6. To access the application during development, visit http://localhost:8181/ in your web browser.
7. When you are done, use the following command in the terminal to clean up the environment:
```bash
docker compose down -v
```


<br>

## Develop without an IDE
To develop without an IDE, follow these steps:

1. Run the following command in the terminal to start the entire application:
```shell
./dev docker compose up --build
```
2. Access the application at http://localhost:8181/ in your web browser.
3. Make changes and repeat the process from step 1 as needed.
4. You can launch the PHP CLI by running the following command in the terminal:
```shell
./dev docker compose run java-proxy bash 
```
5. Run tests by executing the following command in the terminal:
```shell
docker compose run --build --user devbox:devbox java-proxy test
```
6. When you are done, use the following command in the terminal to clean up the environment:
```bash
docker compose down -v
```


<br>

## Tech Stack
* Backend
  - Java 
  - Spring Boot
  - Junit
  - Mockito
  - MultithreadedTC
  - Gradle
  - Lombok
  - Guava
  - Apache Commons IO
  - PHP 8
* Frontend
  - Java Script
  - TypeScript
  - React.js
  - Material UI
* Docker
* WebSocket
* STOMP

