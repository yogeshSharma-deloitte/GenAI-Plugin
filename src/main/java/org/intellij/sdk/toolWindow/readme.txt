There is a Springboot Project named as IntelliBotBackend.  This contains Springboot APIs.

Below are the Folder structuring defined :

1. Folder Structure starts from com.example.intelliJBotBackend followed by the controller, service, repository, entity folders.

2. For the test cases we have similar project structure just instead of main we have test package for the test cases.

Exception :
1. ALl the exception classes will be under com.example.IntelliBotBackend.exception

Entity :

1. It uses persistence package javax.persistence
2. ALl the entity classes will be under com.example.IntelliBotBackend.entity

Repository :

1. It uses repository as a {JPA}.
2. ALl the Repository classes will be under com.example.IntelliBotBackend.repository

Service:
1. ALl the service classes will be under com.example.IntelliBotBackend.service

Controller:
1. ALl the controller classes will be under com.example.IntelliBotBackend.controller

Note :

1. To inject other necessary classes used @Autowired.
