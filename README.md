# WebTestFramework

[![](https://jitpack.io/v/rayzhouzhj/WebTestFramework.svg)](https://jitpack.io/#rayzhouzhj/WebTestFramework)

## Description:
Test framework for Web testing integrated with TestNG and Extent Report.

### Run Below command to create docker containers before test execution:
```
docker-compose up --scale selenium-chrome=5 --scale selenium-firefox=3
```

### Config below properties to setup the test framework(config.properties):
```
############################## WEB ##########################################
# If more than one browser is provided in BROWSER_TYPE
# test will be run on different browsers in parallel thread
BROWSER_TYPE=firefox,chrome
# Selenium hub in docker server
HOST_URL=http://localhost:4444/wd/hub

####################### FRAMEWORK ###########################################
FRAMEWORK=testng
THREAD_COUNT=3
DATAPROVIDER_THREAD_COUNT=3
MONGODB_SERVER=localhost
MONGODB_PORT=27017
MAX_RETRY_COUNT=0

######################## TEST ###############################################
#EXCLUDE_GROUPS=Installation
INCLUDE_GROUPS=DEBUG

######################## DEBUG ###############################################
# DRIVER_HOME ==> Path for webdriver, e.g. chromedriver and geckodriver
DRIVER_HOME=/Users/ray.zhou/Documents/WebDriver
# With debug mode ON, browser will be launched locally using driver in DRIVER_HOME
DEBUG_MODE=OFF
```

### Run below command to start test execution:
```
URL=<your testing url> mvn clean test -Dtest=WebRunner
```

### Visual Testing
#### Install [pixelmatch](https://github.com/mapbox/pixelmatch)
```
npm install pixelmatch
```

#### How to use it
```java
PixelMatch.PixelMatchResult matchResult = new PixelMatch()
                                             .match(actualScreenPNG, expectedResultPNG, outputPNGFile);

if (matchResult.IsMatched) {
    logger.logPassWithScreenshot("Passed");
} else {
    logger.logFailWithImage("error in visual testing", output);
}
```
#### Pixelmatch result in report
<img src="https://github.com/rayzhouzhj/WebTestFramework/blob/master/for-readme/report.png" width="300" style="padding-left: 50px"><img src="https://github.com/rayzhouzhj/WebTestFramework/blob/master/for-readme/pixelmatchresult.png" width="300">

