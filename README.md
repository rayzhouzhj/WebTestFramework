# WebTestFramework

## Description:
Test framework for Web testing integrated with TestNG and Extent Report.

### Run Below command to create docker containers before test execution:
```
docker-compose scale selenium-chrome=5 selenium-firefox=3
```

### Config below properties to setup the test framework:
```
############################## WEB ##########################################
BROWSER_TYPE=chrome
HOST_URL=http://localhost:4444/wd/hub

####################### FRAMEWORK ###########################################
FRAMEWORK=testng
THREAD_COUNT=3
DATAPROVIDER_THREAD_COUNT=3
MONGODB_SERVER=localhost
MONGODB_PORT=27017
MAX_RETRY_COUNT=0
```

### Run below command to start test execution:
```
mvn clean test -Dtest=WebRunner
```
