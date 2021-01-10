# WebTestFramework

[![](https://jitpack.io/v/scmp-contributor/WebTestFramework.svg)](https://jitpack.io/#scmp-contributor/WebTestFramework)

## Description:
Test framework for Web testing integrated with TestNG and Extent Report.


### Config below properties to setup the test framework(config.properties):
```
############################## WEB ##########################################
# If more than one browser is provided in BROWSER_TYPE
# test will be run on different browsers in parallel thread
# if `random` is set, chrome and firefox will be random assigned unless
# the test is annotated by FirefoxOnly or ChromeOnly
# Available options: chrome,firefox,random
BROWSER_TYPE=random
# Selenium hub in docker server
HOST_URL=http://localhost:4444/wd/hub

####################### FRAMEWORK ###########################################
FRAMEWORK=testng
THREAD_COUNT=3
DATAPROVIDER_THREAD_COUNT=3
MONGODB_SERVER=localhost
MONGODB_PORT=27017
MAX_RETRY_COUNT=0
REMOVE_FAILED_TEST_B4_RETRY=true

######################## TEST ###############################################
#EXCLUDE_GROUPS=Installation
INCLUDE_GROUPS=DEBUG

######################## DEBUG ###############################################
# DRIVER_HOME ==> Path for webdriver, e.g. chromedriver and geckodriver
DRIVER_HOME=/Users/ray.zhou/Documents/WebDriver
# With debug mode ON, browser will be launched locally using driver in DRIVER_HOME
LOCAL_EXECUTION=OFF
```

