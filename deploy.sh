lein clean
lein javac
lein pom
mvn dependency:copy-dependencies -DoutputDirectory=target/war/WEB-INF/lib
mkdir -p target/war/WEB-INF/classes
cp -R src/* config/* target/war/WEB-INF/classes
cp -R target/classes/* target/war/WEB-INF/classes
cp weblogic.xml target/war/WEB-INF
cp web.xml target/war/WEB-INF
count=`git rev-list HEAD | wc -l | sed -e 's/ *//g' | xargs -n1 printf %04d`
commit=`git show --abbrev-commit HEAD | grep '^commit' | sed -e 's/commit //'`
buildno=b"$count.$commit"
jar cvf target/facephi-service-$1+$buildno.war -C target/war WEB-INF
echo WAR generado en target/facephi-service-$1+$buildno.war
