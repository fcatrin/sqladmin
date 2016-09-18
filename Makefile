include config.mak
SCRIPT=sqladmin/sqladmin

all: 
	make -C src

clean:
	rm -f src/sqladmin/*.class
	rm -f sqladmin/*.class
	
install:
	echo "#/bin/sh" > ${SCRIPT}.sh
	echo "SQLADMIN="${INSTALL_DIR}"/sqladmin" >> ${SCRIPT}.sh
	echo -e "CLASSPATH=\044CLASSPATH:"${CLASS_XML}":"${INSTALL_DIR} >> ${SCRIPT}.sh
	echo "JDBC_DIR="${JDBC_DIR} >> ${SCRIPT}.sh
	cat ${SCRIPT}.in >> ${SCRIPT}.sh

	cp -R sqladmin ${INSTALL_DIR}
	rm -f ${INSTALL_DIR}/sqladmin/*.in
	chown root.root -R ${INSTALL_DIR}/sqladmin
	ln -sf ${INSTALL_DIR}/sqladmin/sqladmin.sh /usr/local/bin/sqladmin
	chmod 755 /usr/local/bin/sqladmin