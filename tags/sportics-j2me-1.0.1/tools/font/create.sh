#!/bin/sh

. ./env.sh

BASE=`pwd`
OUTDIR=${BASE}/out/${1}

CHARSET1=char1.txt
CHARSET2=char2.txt

if [ -d OUTDIR ]; then
  rm -rf ${OUTDIR}/*
fi

echo OUTDIR: ${OUTDIR}
echo SIZE:  ${1}
echo SERIF: ${SERIF}
echo SANS:  ${SANS}

tr -d '\n' < ${CHARSET1} > ${CHARSET1}.tmp
tr -d '\n' < ${CHARSET2} > ${CHARSET2}.tmp
mv ${CHARSET1}.tmp ${CHARSET1}
mv ${CHARSET2}.tmp ${CHARSET2}

#java -jar microfont-maker.jar ${SERIF} ${CHARSET2} utf-8 ${1} ${COLOR} ${OUTDIR} -dir
java -jar microfont-maker.jar ${SANS} ${CHARSET2} utf-8 ${1} ${COLOR} ${OUTDIR} -dir
java -jar microfont-maker.jar ${SANS} ${CHARSET1} utf-8 ${1} ${COLOR} ${OUTDIR} -dir

