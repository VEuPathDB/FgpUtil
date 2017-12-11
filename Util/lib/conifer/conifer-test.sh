#!/bin/bash

_CTX=trichdb.b35
_PROJECTHOME=/var/www/TrichDB/${_CTX}/project_home
_GUSHOME=/var/www/TrichDB/${_CTX}/gus_home
_PROJECT=TrichDB
_COHORT=ApiCommon
_HOSTNAME=sa.vm.trichdb.org

export PATH=/usr/local/bin:/usr/bin:/bin

set -e
set -x

unset PROJECT_HOME
unset GUS_HOME

for CMD in /usr/local/bin/conifer ${_GUSHOME}/bin/conifer; do

echo '##### EBRC WEBSITE, NO ENV ##################'
if [[ -d "${_PROJECTHOME}_" ]]; then
  mv "${_PROJECTHOME}_" "$_PROJECTHOME"
fi

$CMD install $_HOSTNAME

mv "$_PROJECTHOME" "${_PROJECTHOME}_"

$CMD seed $_HOSTNAME
$CMD configure $_HOSTNAME

echo '##### GUS COMP., NO ENV ####################'

if [[ -d "${_PROJECTHOME}_" ]]; then
  mv "${_PROJECTHOME}_" "$_PROJECTHOME"
fi

$CMD install ApiCommonWebsite \
  --project-home $_PROJECTHOME \
  --gus-home $_GUSHOME

mv "$_PROJECTHOME" "${_PROJECTHOME}_"

$CMD seed ApiCommonWebsite --project $_PROJECT --gus-home $_GUSHOME
$CMD configure ApiCommonWebsite --project $_PROJECT \
  --gus-home $_GUSHOME \
  --site-vars /var/www/${_HOSTNAME}/etc/conifer_site_vars.yml \
  --webapp-ctx ${_CTX} \
  --hostname $_HOSTNAME



echo '##### FREESTYLE #############################'
if [[ -d "${_PROJECTHOME}_" ]]; then
  mv "${_PROJECTHOME}_" "$_PROJECTHOME"
fi

$CMD install \
  --cohort $_COHORT \
  --project-home $_PROJECTHOME \
  --gus-home $_GUSHOME

mv "$_PROJECTHOME" "${_PROJECTHOME}_"

$CMD seed \
  --project $_PROJECT \
  --cohort $_COHORT \
  --gus-home $_GUSHOME

$CMD configure \
  --project $_PROJECT \
  --cohort $_COHORT \
  --gus-home $_GUSHOME \
  --site-vars /var/www/${_HOSTNAME}/etc/conifer_site_vars.yml \
  --webapp-ctx ${_CTX} \
  --hostname $_HOSTNAME


echo '##### GUS COMP., WITH ENV ###################'

if [[ -d "${_PROJECTHOME}_" ]]; then
  mv "${_PROJECTHOME}_" "$_PROJECTHOME"
fi

set +x
source /var/www/${_HOSTNAME}/etc/setenv
set -x

$CMD install ApiCommonWebsite

mv "$_PROJECTHOME" "${_PROJECTHOME}_"

$CMD seed ApiCommonWebsite --project $_PROJECT
# Project is strictly require but we should consider if it can be optional like hostname and webapp-ctx.
# site-vars is required to provide final values (can be empty)
# hostname and webapp-ctx are optional to Conifer but typically required for
# most configurations so there are CLI conveniences args for them so you don't
# have to write out '-e hostname=foo' syntax.
# However, the generated seed file will include hostname and webapp_ctx as required
# vars so if you set it there you don't need the CLI args.
$CMD configure ApiCommonWebsite --project $_PROJECT \
  --site-vars /var/www/${_HOSTNAME}/etc/conifer_site_vars.yml \
  --webapp-ctx ${_CTX} \
  --hostname $_HOSTNAME

done

#######################################################
if [[ -d "${_PROJECTHOME}_" ]]; then
  mv "${_PROJECTHOME}_" "$_PROJECTHOME"
fi
