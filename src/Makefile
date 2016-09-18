include ../config.mak

SCRIPT=../sqladmin/sqladmin

all: ../sqladmin/Main.class


../sqladmin/Main.class:
	javac -classpath ${CLASSPATH}:${CLASS_XML} sqladmin/Main.java
	mv sqladmin/*.class ../sqladmin
	


	