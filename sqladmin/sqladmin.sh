#/bin/sh
SQLADMIN=/usr/local/libexec/sqladmin
CLASSPATH=$CLASSPATH:/usr/local/lib/sunXML/jaxp.jar:/usr/local/lib/sunXML/crimson.jar:.:/usr/local/libexec
JDBC_DIR=/usr/local/lib/jdbc
export CLASSPATH=$CLASSPATH:$SQLADMIN
for i in $JDBC_DIR/*.jar
	do CLASSPATH=$CLASSPATH:$i
done
echo $CLASSPATH
java sqladmin.Main
