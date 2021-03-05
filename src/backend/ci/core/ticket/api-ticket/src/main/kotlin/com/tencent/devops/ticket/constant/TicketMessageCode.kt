package com.tencent.devops.ticket.constant

/**
 * 流水线微服务模块请求返回状态码
 * 返回码制定规则（0代表成功，为了兼容历史接口的成功状态都是返回0）：
 * 1、返回码总长度为7位，
 * 2、前2位数字代表系统名称（如21代表蓝盾平台）
 * 3、第3位和第4位数字代表微服务模块（00：common-公共模块 01：process-流水线 02：artifactory-版本仓库 03:dispatch-分发 04：dockerhost-docker机器
 *    05:environment-蓝盾环境 06：experience-版本体验 07：image-镜像 08：log-蓝盾日志 09：measure-度量 10：monitoring-监控 11：notify-通知
 *    12：openapi-开放api接口 13：plugin-插件 14：quality-质量红线 15：repository-代码库 16：scm-软件配置管理 17：support-蓝盾支撑服务
 *    18：ticket-证书凭据 19：project-项目管理 20：store-商店 21： auth-权限 22:sign-签名服务）
 * 4、最后3位数字代表具体微服务模块下返回给客户端的业务逻辑含义（如001代表系统服务繁忙，建议一个模块一类的返回码按照一定的规则制定）
 * 5、系统公共的返回码写在CommonMessageCode这个类里面，具体微服务模块的返回码写在相应模块的常量类里面
 * @since: 2020-04-24
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
object TicketMessageCode {
    const val CERT_ID_TOO_LONG = "2118001" // 证书名字不能超过128个字符

    const val CREDENTIAL_NAME_ILLEGAL = "2118101" // 凭证名称必须是汉字、英文字母、数字、连字符(-)、下划线(_)或英文句号(.)
    const val CREDENTIAL_ID_ILLEGAL = "2118102" // 凭证标识必须是英文字母、数字或下划线(_)
    const val CREDENTIAL_NOT_FOUND = "2118103" // 凭证{0}不存在
    const val CREDENTIAL_FORMAT_INVALID = "2118104" // 凭证格式不正确
    const val CREDENTIAL_NAME_TOO_LONG = "2118105" // 凭证名字超过32个字符
    const val CREDENTIAL_ID_TOO_LONG = "2118106" // 凭证ID超过32个字符
    const val CREDENTIAL_EXIST = "2118107" // 凭证{0}已存在
}
