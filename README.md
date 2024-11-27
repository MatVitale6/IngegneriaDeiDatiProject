<!-- PROJECT SHIELDS -->
[![Contributors][contributors-shield]][contributors-url]
[![Stargazers][stars-shield]][stars-url]

# Ingegneria Dei Dati

## About The Project

In this repository, you will find the development of various homework assignments for the Data Engineering course as well as the final project for the exam. Each task and project focuses on applying key principles of data engineering, leveraging advanced tools and frameworks to solve practical challenges.

## Getting Started

### Built With

* [![Spring-Boot][Spring-Boot]][Spring-Boot url]
* [![Apache-Lucene][Apache-Lucene]][Apache-Lucene url]

### Prerequisites

* Java 17
* Apache Lucene 9.7.0

### Installation

1. Clone the repo

    ```sh
    git clone https://github.com/MatVitale6/IngegneriaDeiDatiProject.git
    ```

2. Run the app through Maven Wrapper (or with classical Maven if it's installed)

    ```sh
    ./mvnw clean install
    ```

## System Overview (HW2)

This system is a data discovery application designed to index and search scientific documents for relevant information. It leverages **Apache Lucene** for indexing and search functionality, and **Spring Boot** to provide a robust framework architecture. The frontend is implemented using **Thymeleaf** to deliver dynamic and user-friendly interfaces.

### Features

1. **Dynamic Resource Management**
    * Supports multiple resource types like html and json.
    * Configurable analyzers for different fields via centralized `AnalyzerFactory`.

2. **Indexing System**
    * **JSON and HTML Indexers**: Processes files of respective types and extracts relevant fields.
    * Supports field-level extraction such as titles, captions, content, authors, footnotes, etc.
    * Tracks progress dynamically through a `ProgressService`:

3. **Search System**
    * Provides a REST API endpoint for querying indexed documents.
    * Supports multiple fields (e.g., title, content, abstract).

4. **Performance and Logging**
    * Logs statistics such as processing times for files and tables.
    * Tracks and logs empty fields in the processed data to ensure data quality.

### Workflow

1. **Indexing**
    * At startup, the system checks for incomplete indexing tasks by verifying the presence of flag files.
    * Executes indexing for all incomplete resource types.
    * Tracks the progress dynamically and updates the frontend.

2. **Search**
    * Users provide a query and select the resource type (html or json).
    * The system parses the query and performs a search on the respective index.
    * Returns a list of articles or tables, including the matched field, relevance score, and a link to the resource.

## Embeddings (HW3)

To enable *BERT Embedding Indexing feature* navigate to `application.properties` and set the flag:

```properties
indexing.use.embeddings=true
```

The embeddings feature relies on a dedicated server that hosts a Sentence Transformer model from Hugging Face called `bert-base-nli-mean-tokens`. This model is pre-trained to generate high-quality embeddings for natural language processing tasks, enabling the indexing system to process and compare text data effectively.

To set up this server, you need Docker installed and running on your system. Docker provides a containerized environment that simplifies the deployment process, ensuring consistency and reducing dependency conflicts. The server image is pre-configured to load the `bert-base-nli-mean-tokens` model into memory, making it ready to handle embedding requests efficiently as soon as it starts.

---

### Running BERT Embedding Server

1. **Install Docker Desktop** (if you're using Windows):

   * Download and install Docker Desktop from the official website: [Docker Desktop](https://www.docker.com/products/docker-desktop/).
   * Ensure Docker Desktop is running before proceeding.

2. **Get the Image**:
    * Obtain the `bert-server.tar` file by running:

        ```bash
        docker pull hermannt1/bert-server:latest
        ```

    * Check if the image is loaded:

        ```bash
        docker images
        ```

3. **Run the container**
    * Start the container with:
  
        ```bash
        docker run --rm -d -p 5000:5000 bert-server:latest
        ```

    * The BERT Server will be running on `http://localhost:5000`.
    * Verify the server is working correctly making a GET to `http://localhost:5000/health`. You should get a response like:

        ```json
        {"status":"ok"}
        ```

Once the server is running, simply launch the application. The `EmbeddingServerService` class will handle all the necessary tasks for you, such as starting the Docker container automatically. All you need to do is update the `application.properties` file to enable the embedding feature.

## Partecipants

<a href="https://github.com/MatVitale6/IngegneriaDeiDatiProject/graphs/contributors">
<img src="https://contrib.rocks/image?repo=MatVitale6/IngegneriaDeiDatiProject"/>
</a>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/MatVitale6/IngegneriaDeiDatiProject.svg?style=for-the-badge
[contributors-url]: https://github.com/MatVitale6/IngegneriaDeiDatiProject/graphs/contributors
[stars-shield]: https://img.shields.io/github/stars/MatVitale6/IngegneriaDeiDatiProject.svg?style=for-the-badge
[stars-url]: https://github.com/MatVitale6/IngegneriaDeiDatiProject/stargazers

[product-screenshot]: images/screenshot.png
[Spring-Boot]: https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white
[Spring-Boot url]: https://spring.io/projects/spring-boot
[Apache-Lucene]: https://img.shields.io/badge/-ApacheLucene-019B8F?style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAABRCAYAAAAaXK5BAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAARkSURBVHgB7Z2NUdwwEIXf3aSA0IFTQdJBXEKoIEMHpIOkg6QDOgA62HQAFeBUEDq4SBeLuTuM785+K61kfTNg/mYA39O+1WolrzabzQ2Ar1gmV6vV6gYk3L1s3OUJPDr3932AImv3du1/EZbJA7h8ApdHKLN2Cnt21yssEPe/swXQgotAGR8B/I0Qd/mFZSHg8xFc2AJ9xTp84ERwHeMXGkIjvFItoB+YqqwPPr90b89YBgIiLgH0L/578BBEYE8ATnGdu/zAMujApQEX9QTQcxgBvAh+IpL6EvJcE8D/rN/4eulWoJHrsBPADhEYFEA/NbxEuWiE1xY8NCLUIG9FgNKnhgIiLgFswSXabGx95PvfUWaVsAMXdgXwNyIxKoBCrUAjvLL9XxCJYxEglEu/oRw0wis7ApixgC2FTQ2p4dX5vy/+MAXw0EfeKJwkgB6/YFTC1ND6CmDUcvzJAuirhCVYgXUBREsAKwScBdxuuLAFNcoKlVm4F8x3ADXg4GcoF4jIOTlA5YA+AWzAI/pyfBXAPFpwie7/VQDzYPu1IDJVAPP4DC7VAjIj2wJQoApgIgotYH+QgCqA6TTgIkhAFcB0WnBJ0pFdBTAd6hJwjBbwIaoAptOChyARVQATUKjXR2kBH6IKYBrZF4ACVQDTyL4AFKgCmAYzAnR9r0USqgDORKEFLJn/e6oAzqcY//e8gxK+U8ZdviA97GNWsu4BPEQzAlhpImWHWGoCmKoAFFATgKGjZwRcGvAQJEY1B3AiuHOXO6RFQKK0BNATIwlMaQXsbWBFJYAedQEktgLrh0B0SEyUaWBCK2A3WTITwGhnAIwRsw6QwgoEXKgtYDBANAEksgLaTVZoATOxBSxqJTCyFbCbLBtwERggRSk4lhWwR1gLLsuygEBEKxBwYbaAJWkBHyLJYlAkK7C8DdzE6PekXA3UtALqGnupCaAnmQCUrYBdYi1qBXCXpP0AilYg4FJcAShgoSFEwwoEXIr0f09yAShYgcYIYwrA1BlAJlrCyFZAffEVjoEVGMJSTyDLCtgjrNgE0GNGAEQrEHBhJoBmCkABU13BJCtgj7AGPJKcATCGxbbwOVZAHWEKLWACY5gTwEwrqP5/JiY3hsywAgGXFkRSt4APYXln0BQrsPwcAIFBzApgghVobLJswSN5C/gQpvcGnmkF1BvcPwmcuQIoMEgOm0NPtQIBl+ITQI/a5lAW3grcaPQiuD3yowIuLbg8uf8Dxuiy2B5+ghXk8CAoizzmdD7AmBXk8CAoi9xlI4AjswL2g6DYLWBWecjqhJARKxBwWcLo39pmjkfEDFmB5QYQq2zvWXYCGLACjSXWJSSA9/5dlodEHViBRotVi/LJMwLsEKxAQEShBcwkYWEqWwHsWEH1//OR8IH5SuAYvRWwYR8Da5GXdZN6UORrGpTPy8CpTw7doW8B+4vyuQgzpxoB9lnE/H932lwFsM9iCkCBKoB9lpAA3u9+UgWwz+IiwD/gJHOrDPZxTAAAAABJRU5ErkJggg==
[Apache-Lucene url]: https://lucene.apache.org/

