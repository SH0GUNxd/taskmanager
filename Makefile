SRC     = src/Task.java src/TaskManager.java src/ConsoleUI.java src/ApiServer.java src/Main.java
OUT     = out
PORT    = 8080

.PHONY: all build test test-unit test-api run web clean

all: build

build:
	mkdir -p $(OUT)
	javac -d $(OUT) $(SRC)

test-unit: build
	javac -cp $(OUT) -d $(OUT) tests/TaskManagerTest.java
	java -cp $(OUT) taskmanager.TaskManagerTest

test-api: build
	javac -cp $(OUT) -d $(OUT) tests/ApiServerTest.java
	java -cp $(OUT) taskmanager.ApiServerTest

test: build
	javac -cp $(OUT) -d $(OUT) tests/TaskManagerTest.java tests/ApiServerTest.java
	java -cp $(OUT) taskmanager.TaskManagerTest
	java -cp $(OUT) taskmanager.ApiServerTest

run: build
	java -cp $(OUT) taskmanager.Main

web: build
	java -cp $(OUT) taskmanager.Main --web $(PORT)

jar: build
	jar --create --file TaskManager.jar --main-class taskmanager.Main -C $(OUT) .

clean:
	rm -rf $(OUT) TaskManager.jar
