ktor-chat-server
========
A chat server based on the excellent Ktor coroutine framework!

API
========
The chat server runs on http://localhost:8081 and supports the following REST API: 

 1. GET `/messages`

     list 100 most recent messages, sorted by 'timestamp' posted to the chat server.

     ```
     curl -H "Content-Type: application/json" http://localhost:8081/messages

     {
       "messages: [
         {"timestamp": 1491345710.18, "user": "superman", "text": "hello"},
         {"timestamp": 1491345713.18, "user": "batman", "text": "hello"}
       ]
     }

     ```

 2. POST `/message` 

     a request to post the given message. 
     when the message is processed by the server a unix timestamp is recorded with each message.

     ```
     curl -X POST -H "Content-Type: application/json" --data '{"user":"superman", "text":"hello"}' http://localhost:8081/message

     {
       "ok": true
     }
     ```

 3. GET `/users`

     a request to return a set of users that have posted messages so far.

     ```
     curl -H "Content-Type: application/json" http://localhost:8081/users

     {
       "users": [
         "superman", "batman"
       ]
     }
     ```
