#!/bin/bash
echo "Start installing the agent..."
t=`date +"%Y-%m-%d_%H-%M-%S"`
workspace=`pwd`
user=${USER}
agent_id='##agentId##'

function initArch() {
  ARCH=$(uname -m)
  case $ARCH in
    aarch64) ARCH="arm64";;
    arm64) ARCH="arm64";;
    mips64) ARCH="mips64";;
    *) ARCH="";;
  esac
}

function getServiceName()
{
  echo "devops_agent_"${agent_id}
}

function unzip_jre()
{
  echo "start unzipping jre package"
  if [[ -d "jre" ]]; then
    echo "jre already exists, skip unzip"
    return
  fi
  unzip -q -o jre.zip -d jre
}

exists()
{
  command -v "$1" >/dev/null 2>&1
}

function download_agent()
{
  echo "start download agent install package"
  if [[ -f "agent.zip" ]]; then
    echo "agent.zip already exist, skip download"
    return
  fi
  if exists curl; then
    curl -H "X-DEVOPS-PROJECT-ID: ##projectId##" -o agent.zip "##agent_url##?arch=${ARCH}"
    if [[ $? -ne 0 ]]; then
      echo "Fail to use curl to download the agent, use wget"
      wget --header="X-DEVOPS-PROJECT-ID: ##projectId##" -O agent.zip "##agent_url##?arch=${ARCH}"
    fi
  elif exists wget; then
    wget --header="X-DEVOPS-PROJECT-ID: ##projectId##" -O agent.zip "##agent_url##?arch=${ARCH}"
  else
    echo "Curl & wget command don't exist, download fail"
    exit 1
  fi
}

function installAgentService()
{
  echo "install agent service with user ${user}"
  grep_result=$(grep "${service_name}" /etc/rc.d/rc.local)
  if test -x "/etc/rc.d/rc.local" ; then
    if [[ -z "$grep_result" ]]; then
      echo "cd ${workspace} && ./devopsDaemon & # ${service_name}" >> /etc/rc.d/rc.local
      echo "add to rclocal"
    else
      echo "already add to rclocal"
    fi
  fi

  cd ${workspace}
  chmod +x *.sh
  ${workspace}/start.sh
}

function uninstallAgentService()
{
  echo "uninstall agent service"
  grep_result=$(grep "${service_name}" /etc/rc.d/rc.local)
  if test -x "/etc/rc.d/rc.local" ; then
    if [[ -z "$grep_result" ]]; then
        echo "already remove from rclocal"
    else
        sed -i "/${service_name}/d" "/etc/rc.d/rc.local"
        echo "removal done"
    fi
  fi

  cd ${workspace}
  chmod +x *.sh
  ${workspace}/stop.sh
}

function writeSSHConfig()
{
    config_file=$HOME/.ssh/config
    if [[ ! -d $HOME/.ssh ]];then
        mkdir -p $HOME/.ssh
    fi

    if [[ -f ${config_file} ]];then

        if [[ $(cat ${config_file}| grep "\-svn.tencent.com"  | wc -l) -lt 1 ]];then
            echo "" >> ${config_file}
            echo "Host *-svn.tencent.com" >> ${config_file}
            echo "StrictHostKeyChecking no" >> ${config_file}
            echo "Port 22" >> $config_file
        fi
        if [[ $(cat ${config_file}| grep "\-scm.tencent.com"  | wc -l) -lt 1 ]];then
            echo "" >> ${config_file}
            echo "Host *-scm.tencent.com" >> ${config_file}
            echo "StrictHostKeyChecking no" >> ${config_file}
            echo "Port 22" >> ${config_file}
        fi
        if [[ $(cat $config_file| grep "\-cd1.tencent.com"  | wc -l) -lt 1 ]];then
            echo "" >> ${config_file}
            echo "Host *-cd1.tencent.com" >> ${config_file}
            echo "StrictHostKeyChecking no" >> ${config_file}
            echo "Port 22" >> ${config_file}
        fi
        if [[ $(cat ${config_file}| grep "Host git.code.oa.com"  | wc -l) -lt 1 ]];then
            echo "" >> ${config_file}
            echo "Host git.code.oa.com" >> ${config_file}
            echo "StrictHostKeyChecking no" >> ${config_file}
            echo "HostName git.code.oa.com" >> ${config_file}
            echo "Port 22" >> ${config_file}
        fi
    else
      cat > ${config_file} <<EOF
Host *-svn.tencent.com
StrictHostKeyChecking no
Port 22
Host *-scm.tencent.com
StrictHostKeyChecking no
Port 22
Host *-cd1.tencent.com
StrictHostKeyChecking no
Port 22
Host git.code.oa.com
StrictHostKeyChecking no
HostName git.code.oa.com
Port 22
EOF
    fi
}

# if [[ "${workspace}" = ~ ]]; then
#  echo 'agent should not install in root of user home directory'
#  echo 'please run install script in an empty directory with full permission'
#  exit 1
# fi

cd ${workspace}

if [[ ! -f "agent.zip" ]]; then
  initArch
  download_agent
  unzip -o agent.zip
fi

unzip_jre

os=`uname`
arch1=`uname -m`
echo "OS: $os"
echo "ARCH: ${arch1}"

echo "check java version"
jre/bin/java -version

echo "check and write ssh config"
writeSSHConfig

service_name=`getServiceName`

if [[ "$user" = "root" ]]; then
    uninstallAgentService
    installAgentService
else
    cd ${workspace}
    chmod +x *.sh
    ${workspace}/start.sh
fi
