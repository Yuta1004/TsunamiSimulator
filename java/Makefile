JAVA := java
JAVAC := javac
JAR := jar
JLINK := $(JAVA_HOME)/bin/jlink

OPTS := -p $(JAVAFX_PATH)/lib --add-modules javafx.controls
JLINK_OPTS := --compress=2 --module-path $(JAVAFX_PATH)/jmods --add-modules javafx.controls

SRCS := $(wildcard *.java)
MANIFEST := MANIFEST.MF

run: Main.class
	$(JAVA) $(OPTS) Main

Main.class: $(SRCS)
	$(JAVAC) $(OPTS) Main.java

dist: $(SRCS)
	make clean
	mkdir classes dist
	$(JAVAC) $(OPTS) -d classes Main.java
	$(JAR) cvfm dist/TsunamiSimulator.jar $(MANIFEST) -C classes .
	$(JLINK) $(JLINK_OPTS) --output dist/runtime
	echo "run:\n\truntime/bin/java -jar TsunamiSimulator.jar" > dist/Makefile
	rm -rf classes

clean:
	rm -rf *.class */*.class classes dist *.args
