JAVA = java
JAVAC = javac
OPTS =
SRCS = $(wildcard *.java)

Main.class: $(SRCS)
	$(JAVAC) Main.java

run: Main.class
	$(JAVA) Main

clean:
	rm -rf *.class
