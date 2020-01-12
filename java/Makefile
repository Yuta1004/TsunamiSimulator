JAVA = java
JAVAC = javac
JAR = jar
OPTS =
SRCS = $(wildcard *.java)
MANIFEST = MANIFEST.MF

run: Main.class
	$(JAVA) Main

Main.class: $(SRCS)
	$(JAVAC) Main.java

dist:
	make clean
	mkdir classes dist
	$(JAVAC) -d classes Main.java
	$(JAR) cvfm TsunamiSimulator.jar $(MANIFEST) -C classes .
	mv TsunamiSimulator.jar dist
	rm -rf classes

clean:
	rm -rf *.class */*.class classes dist
