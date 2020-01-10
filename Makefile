JAVA = java
JAVAC = javac
OPTS =

Main.class: Main.java
	$(JAVAC) Main.java

run: Main.class
	$(JAVA) Main

clean:
	rm -rf *.class
