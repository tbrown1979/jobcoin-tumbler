# jobcoin-tumbler

This project creates a mixer for a fake cryptocurrency called `Jobcoin`.

To run: open with `sbt` and type `run`

This starts a server up on port `8080` and exposes `1` endpoint.

* **URL**

  `/addresses`

* **Method:**

  `POST`
  
* **Body**

  ```json
  ["array", "of", "your", "addresses"]
  ```

* **Success Response:**

  * **Code:** 200 <br />
  * **Content:** 

   ```
     {
       "value": "d3fa70be-7eea-4970-af33-0dcd910cdd09"
     }
   ```
   
##### Libraries used:

This project utilizes Cats, fs2, Refined, Circe, and http4s.
