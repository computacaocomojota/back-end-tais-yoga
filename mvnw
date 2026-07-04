#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ] ; then

  if [ -f /usr/local/etc/mavenrc ] ; then
    . /usr/local/etc/mavenrc
  fi
  if [ -f /etc/mavenrc ] ; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ] ; then
    . "$HOME/.mavenrc"
  fi

fi

# OS specific support. $var _must_ be set to either true or false.
cygwin=false;
msys=false;
darwin=false;
nonstop=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) msys=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_HOME" ] ; then
             if [ -x '/usr/libexec/java_home' ] ; then
               JAVA_HOME=`/usr/libexec/java_home`
               export JAVA_HOME
             fi
           fi
           ;;
  NONSTOP*) nonstop=true ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -x "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin/java" ]; then
    JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
    export JAVA_HOME
  elif [ -x "/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/java" ]; then
    JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home"
    export JAVA_HOME
  elif [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="`which java`"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

if [ -z "$MAVEN_PROJECTBASEDIR" ] ; then
  MAVEN_PROJECTBASEDIR="`pwd`"
fi

MAVEN_OPTS="$MAVEN_OPTS -Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

# Find jar or download
wrapperJar="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -e "$wrapperJar" ]; then
  wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
  echo "Downloading Maven Wrapper jar from $wrapperUrl"
  mkdir -p "$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
  if [ -x "`which curl`" ]; then
    curl -f -L -o "$wrapperJar" "$wrapperUrl"
  elif [ -x "`which wget`" ]; then
    wget -O "$wrapperJar" "$wrapperUrl"
  fi
fi

exec "$JAVACMD" \
  $MAVEN_OPTS \
  -classpath "$wrapperJar" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  $WRAPPER_LAUNCHER "$@"
