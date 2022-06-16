FROM mirrors.tencent.com/tjdk/tencentkona8-tlinux:latest

LABEL maintainer="Tencent BlueKing Devops"

# 操作系统相关
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'alias ll="ls -l"' >> ~/.bashrc && \
    echo "alias tailf=tail -f" >> ~/.bashrc

# yum安装软件
RUN yum clean all && rpm --rebuilddb && yum update -y && \
    yum install -y procps && \
    yum install -y vi && \
    yum install -y vim

# 第三方工具
RUN mkdir -p /data/tools && \
    curl -o /data/tools/arthas.jar https://arthas.aliyun.com/arthas-boot.jar  && \
    curl -o /data/tools/ot-agent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar



