#!/bin/sh

if [ `readlink $0` ]; then
    scriptname=`readlink -f $0`
else 
    scriptname="$0"
fi
scriptdir=`dirname $scriptname`
basedir=`cd $scriptdir; cd ..; pwd`

libdir="$basedir/lib"

appname=@app.name@
[ -n "$FREECAST_APPNAME" ] && appname=$FREECAST_APPNAME

#
# log dir choose
#
# according to the "running mode", logdir should be something like :
#    in dev mode, $basedir/log
#    in daemon mode, /var/log/freecast
#    in client mode, ~/.freecast/log
#
if [ -z "$logdir" ]; then
    # possible directories (order is important)
    logdirs="$basedir/log /var/log/freecast $HOME/.freecast"
    for dir in $logdirs; do 
        logdir=$dir

        if [ -w "$dir" -a -d "$dir" ]; then 
            # log dir already exists, use it
            break;
        fi

        parentdir=`dirname $dir`
        if [ -w "$parentdir" ]; then 
            # parent dir is writeable, create log dir and use it
            echo "create log dir: $logdir"
            mkdir $logdir
            break;
        fi
    done
fi

if [ ! -w "$logdir" -a -d "$logdir" ]; then
    echo "logdir '$logdir' isn't a writeable directory"
    exit 1
fi

#
# base classpath choose
#
# according to the "running mode", base classpath should be something like :
#    in dev mode, $basedir/etc (and nothing else)
#    in daemon mode, /etc/freecast
#    in client mode, ~/.freecast/etc:/etc/freecast
if [ -d "$basedir/etc" ]; then 
    classpath="$basedir/etc"
else 
    classpath="/etc/freecast"
    [ -d "~/.freecast/etc" ] && classpath="$classpath:/etc/freecast"
fi

for jar in `ls $libdir/*.jar`; do
	classpath="$classpath:$jar"
done
for jar in `ls $libdir/linux/*.jar`; do
	classpath="$classpath:$jar"
done

JAVA_CMD=java
[ -n "$JAVA_HOME" ] && JAVA_CMD=$JAVA_HOME/bin/java

$JAVA_CMD -version 2>&1 | egrep '(java version "1.[45]|java full version "kaffe-1.[45])' > /dev/null
if [ $? != 0 ]; then
    echo "A jdk 1.4 is needed to run @app.name@. Check your JAVA_HOME variable." >&2
    exit 1
fi

exec $JAVA_CMD $JAVA_OPTS -cp $classpath -Djava.library.path=$libdir/linux/x86 -Dapp.name=$appname -Dlog.dir=$logdir -Dlib.dir=$libdir @app.mainclass@ $*
