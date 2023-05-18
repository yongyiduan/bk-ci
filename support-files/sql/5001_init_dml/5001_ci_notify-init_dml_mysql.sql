use devops_ci_notify;
SET NAMES utf8mb4;

-- 公共消息默认消息模板
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("13ec2ac58aab4d9bb8a04bbf7a9242f6","STORE_MEMBER_ADD_ATOM","研发商店插件成员添加模板",'[ "EMAIL", "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("168b9b44b7104647b2102355180a2837","UPDATE_TEMPLATE_INSTANCE_NOTIFY_TEMPLATE","使用模板批量更新流水线的通知模板",'[ "WEWORK", "EMAIL" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("1be5b658d4aa465a9f9a1d0af44c8384","QUALITY_AUDIT_NOTIFY_TEMPLATE_V2","质量红线审核消息模板V2",'[ "WEWORK", "EMAIL" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("1dd8f74e334e4c41a20f4187b4e0e992","EXTENSION_RELEASE_SUCCESS_TEMPLATE","扩展服务发布成功消息通知模板",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("20e4ac3f11ff4866b95a82b30b9032a1","STORE_COMMENT_NOTIFY_TEMPLATE","store评论消息通知模板",'[ "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("2d8c427026b049dc98630507f5e3fa38","PIPELINE_SHUTDOWN_SUCCESS_NOTIFY_TEMPLATE_DETAIL","执行成功的详情通知模板代码",'[ "EMAIL", "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("2e9227b122024a5a83c46a9f41b94aad","STORE_COMMENT_REPLY_NOTIFY_TEMPLATE","store评论回复消息通知模板",'[ "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("30c94a920c79453da784124ab41fb068","ATOM_CODECC_QUALIFIED_TEMPLATE","插件代码扫描合格模板",'[ "EMAIL" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("37ef4157cc254021ac7278b70f211f5e","MANUAL_REVIEW_ATOM_NOTIFY_TEMPLATE","人工审核插件通知",'[ "EMAIL", "WEWORK", "WEWORK_GROUP" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("3a39413a67bf404184dbd4e9309ebeb8","QUALITY_NOTIFY_TEMPLATE","质量红线消息模板",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("3ea6c5e18d4b41a3890a150d1df16f4d","IMAGE_EXECUTE_NULL_NOTIFY_TPL","镜像执行找不到镜像通知管理员",'[ "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("3fbf09f0511b424ab4480e18d6674349","STREAM_V2_BUILD_TEMPLATE","Stream构建结束消息模板",'[ "EMAIL" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("4589771c46b248079444a0e18e46a3fe","IMAGE_RELEASE_AUDIT_PASS_TEMPLATE","研发商店镜像通过审核发布",'[ "EMAIL", "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("698634fd3bae40378eeb24c8d6a5c91c","TEMPLATE_RELEASE_AUDIT_REFUSE_TEMPLATE","模板发布审核被拒消息通知模板",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("6a3db6f561f44d4895518e881a3cb53b","PIPELINE_SHUTDOWN_FAILURE_NOTIFY_TEMPLATE","执行失败的通知模板代码",'[ "EMAIL", "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("6c00ffb0de6f485eb301940ca639d04d","TEMPLATE_RELEASE_AUDIT_PASS_TEMPLATE","模板发布审核通过消息通知模板",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("7a64b8cd997b49858964ac34079bf234","QUALITY_AUDIT_NOTIFY_TEMPLATE","质量红线消息模板",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("91fb62ef51a24266ae0946112e094190","GITCI_V2_BUILD_TEMPLATE","工蜂CI-V2构建结束消息模板",'[ "EMAIL" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("92055af946b64850a193e1090865642e","ATOM_CODECC_FAILED_TEMPLATE","插件代码扫描不通过模板",'[ "EMAIL" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("92c4e65e4cd2432eb326789ce503d698","PIPELINE_SHUTDOWN_FAILURE_NOTIFY_TEMPLATE_DETAIL","执行失败的详情通知模板代码",'[ "EMAIL", "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("96c0a1adaca7428eb0b2d69820923d52","EXTENSION_RELEASE_AUDIT_REFUSE_TEMPLATE","研发商店扩展审核拒绝消息",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("a9b46a21ed7949ddb946e5186b9102c1","EXTENSION_RELEASE_FAIL_TEMPLATE","扩展服务发布失败消息通知模板",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("bf0ca37469cf448a86875bb2f11517c5","PIPELINE_STARTUP_NOTIFY_TEMPLATE_DETAIL","启动的详情通知模板代码",'[ "WEWORK", "EMAIL" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("cbdd75de2c7f40a6a3bc1dfd909020a7","STORE_MEMBER_DELETE_ATOM","研发商店插件成员移除模板",'[ "EMAIL", "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("cd7cecc126744f13a60d655d0af116af","PIPELINE_STARTUP_NOTIFY_TEMPLATE","启动的通知模板代码",'[ "WEWORK", "EMAIL" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("cff00f19e6884862b6f9afe0f608e91e","PIPELINE_SHUTDOWN_SUCCESS_NOTIFY_TEMPLATE","执行成功的通知模板代码",'[ "EMAIL", "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("d77a91dd09994c54b14f118956e0d6e9","PIPELINE_TASK_PAUSE_NOTIFY","流水线TASK暂停通知",'[ "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("d7d9e1a773fe4a658f6c51b80da282ea","QUALITY_END_NOTIFY_TEMPLATE","质量红线拦截消息模板",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("dff46cd92eab458b96b25be0d7a61054","ATOM_RELEASE_AUDIT_REFUSE_TEMPLATE","插件发布审核被拒消息通知模板",'[ "WEWORK", "EMAIL" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("e961fd2ccc1c4f5d8eefaac9d51b5cb0","ATOM_RELEASE_AUDIT_PASS_TEMPLATE","插件发布审核通过消息通知模板",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("ea98239a51a74f8db0a012a809bc1116","IMAGE_RELEASE_AUDIT_REFUSE_TEMPLATE","研发商店镜像审核驳回",'[ "EMAIL", "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("f0663e7ac2b74130ada0408bdce8cd20","QUALITY_END_NOTIFY_TEMPLATE_V2","质量红线拦截消息模板V2",'[ "WEWORK", "EMAIL" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("f0949ab70cc04aa5b1bc787d5a47eba7","MANUAL_REVIEW_STAGE_NOTIFY_TEMPLATE","阶段审核通知",'[ "WEWORK", "EMAIL", "WEWORK_GROUP" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ("f5c17a3f5dcd4c9ea7cf7a16a16e7135","ATOM_COLLABORATOR_APPLY_REFUSE","插件协作开发申请被拒的消息通知模板",'[ "EMAIL", "WEWORK" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ('502f34f94a0d4bc7abe7c55f4e93da46','PIPELINE_WEBHOOK_REGISTER_FAILURE_NOTIFY_TEMPLATE','流水线webhook注册失败的通知模板代码','[ "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ('d58e5895dfab4136ac898d93e3bff3ac','PIPELINE_TRIGGER_REVIEW_NOTIFY_TEMPLATE','构建触发审核通知','[ "WEWORK", "EMAIL" ]',1,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) values('7dee6377c27344818836ef1f035a14d1','MANUAL_REVIEW_STAGE_NOTIFY_TO_TRIGGER_TEMPLATE','stage人工审核用于发给触发者','[ "WEWORK" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) values('15a1e5f8642c4d7196270329531bf4cb','MANUAL_REVIEW_STAGE_REJECT_TO_TRIGGER_TEMPLATE','人工审核-reject时发送消息','[ "WEWORK", "WEWORK_GROUP" ]',0,0);
REPLACE INTO  `T_COMMON_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `TEMPLATE_CODE`, `TEMPLATE_NAME`, `NOTIFY_TYPE_SCOPE`, `PRIORITY`, `SOURCE`) VALUES  ('1e7864726dd04311b21019bcceddfee8','PIPELINE_CALLBACK_DISABLE_NOTIFY_TEMPLATE','流水线callback禁用通知模板代码','[ "WEWORK" ]',0,0);
REPLACE INTO  `T_RTX_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`, `SENDER`) values('094a4cb13de54dc38e034002b01ffbeb','502f34f94a0d4bc7abe7c55f4e93da46','system','system','流水线【${pipelineName}】触发器回调注册失败','你刚刚保存的流水线【${pipelineName}】触发器回调注册失败，失败原因：\n${elementNames}\n\n查看详情：${pipelineEditUrl}','2021-11-16 16:40:10','2021-11-16 19:56:08','DevOps');

-- 企业微信流水线执行消息模板
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("0696ed183dca43ceb0963c99710f1f25","1be5b658d4aa465a9f9a1d0af44c8384","system",'',"DevOps","【蓝盾质量红线】拦截通知-待审核","${title}\n所属项目：${projectName}\n触发人：${cc}\n拦截时间：${time}\n${result}\n流水线链接：${url}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("16b3188f363247ec81923fff2ee4cafd","cff00f19e6884862b6f9afe0f608e91e","system",'',"DevOps","蓝盾流水线【${pipelineName}】#${buildNum} 构建成功","✔️${successContent}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("252497da08fe4abdae8fa49e05ee8ea8","cd7cecc126744f13a60d655d0af116af","system",'',"DevOps","蓝盾流水线【${pipelineName}】#${buildNum} 开始执行","【${projectName}】-【${pipelineName}】#${buildNum} 开始执行","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("34a071f8d22f4edaba56624df3a0aae2","dff46cd92eab458b96b25be0d7a61054","system",'',"DevOps","研发商店插件【${name}】V${version} 审核不通过","研发商店插件【${name}】V${version} 审核不通过","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("390987c50179422388dd742924fc4a01","ea98239a51a74f8db0a012a809bc1116","system",'',"DevOps","研发商店镜像【${name}】V${version} 审核不通过","研发商店镜像【${name}】V${version} 审核不通过，驳回原因：${imageStatusMsg}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("48ba65ca230c46f08c6e3ca2da753aee","e961fd2ccc1c4f5d8eefaac9d51b5cb0","system",'',"DevOps","研发商店插件【${name}】V${version} 审核通过，成功发布","研发商店插件【${name}】V${version} 审核通过，成功发布","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("5182f93f61ee416c854b3e4d4e7b0859","d77a91dd09994c54b14f118956e0d6e9","system",'',"DevOps","蓝盾流水线【${BK_CI_PIPELINE_NAME}】#${BK_CI_BUILD_NUM} 构建暂停","【${BK_CI_PROJECT_NAME_CN}】-【${BK_CI_PIPELINE_NAME}】#${BK_CI_BUILD_NUM} 在执行【${taskName}】前暂停，触发人：${BK_CI_START_USER_ID}[去处理 | ${url}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("607d08cf17424ad483d9bbd57da898d1","6a3db6f561f44d4895518e881a3cb53b","system",'',"DevOps","蓝盾流水线【${pipelineName}】#${buildNum} 构建失败","❌${failContent}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("6ea6e1ef451e44059e6d7545892d97f3","bf0ca37469cf448a86875bb2f11517c5","system",'',"DevOps","蓝盾流水线【${pipelineName}】#${buildNum} 开始执行","【${projectName}】-【${pipelineName}】#${buildNum} 开始执行查看详情:${detailUrl}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("756a60da56d345148567fb1465fbb6a7","698634fd3bae40378eeb24c8d6a5c91c","system",'',"DevOps","研发商店模板【${name}】V${version} 审核不通过","研发商店模板【${name}】V${version} 审核不通过","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("7b803d1073014351a8855bcc02629d90","cbdd75de2c7f40a6a3bc1dfd909020a7","system",'',"DevOps","研发商店插件成员移除通知","研发商店的【${storeName}】插件的管理员【${storeAdmin}】已将您从该插件的成员列表中移除","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("8599414eaeec4791a430594147ded587","20e4ac3f11ff4866b95a82b30b9032a1","system",'',"DevOps","【蓝盾研发商店】评论通知","${userId} 评论了 ${storeName}\n评分：${score}\n评论：${commentContent}\n[去回复 | ${url}]","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("895ddf2c5ab949d889d988d818981239","2d8c427026b049dc98630507f5e3fa38","system",'',"DevOps","蓝盾流水线【${pipelineName}】#${buildNum} 构建成功","✔️${successContent}\n查看详情:${detailUrl}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("8a943ecad5e7490bb04ef1bd7c176568","f5c17a3f5dcd4c9ea7cf7a16a16e7135","system",'',"DevOps","【蓝盾研发商店】协作申请被驳回","流水线插件【${atomName}】的管理员【${atomAdmin}】驳回了你的协作申请,驳回原因：${approveMsg}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("930450b9edfe479e8c7504623261584d","37ef4157cc254021ac7278b70f211f5e","system",'',"DevOps","${content}","审核说明：${reviewDesc}\n\n电脑端点击 ${reviewUrl}\n手机端点击 ${reviewAppUrl}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("97f1480ba61b4a83947924c6c9dad3e6","3ea6c5e18d4b41a3890a150d1df16f4d","system",'',"DevOps","${imageCode}(${imageVersion}) 镜像执行异常","[查看构建链接|${url}] ${userId} 的 ${projectCode} 项目下的 ${imageCode}(${imageVersion}) 镜像执行异常，pipelineId:${pipelineId},buildId:${buildId}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("9e982e6afc67440cab33604b7ad772b6","6c00ffb0de6f485eb301940ca639d04d","system",'',"DevOps","研发商店模板【${name}】审核通过，成功发布","研发商店模板【${name}】审核通过，成功发布","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("9f1c741fe5df486193971ef5a659f061","92c4e65e4cd2432eb326789ce503d698","system",'',"DevOps","蓝盾流水线【${pipelineName}】#${buildNum} 构建失败","❌${failContent}\n\n查看详情:${detailUrl}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("aab3cc905f004b9392773d388d64daef","f0949ab70cc04aa5b1bc787d5a47eba7","system",'',"DevOps","项目【${projectName}】下的流水线【${pipelineName}】#${buildNum} 构建Stage审核","构建状态为stage sucess，需要您的审核才执行后续流程\n审核说明：${reviewDesc}\n电脑端点击 ${reviewUrl}\n手机端点击 ${reviewAppUrl}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("ac07cfe86b5040f7abb1280942e2c57c","3a39413a67bf404184dbd4e9309ebeb8","system",'',"DevOps","【蓝盾质量红线】审核通知","${pipelineName}(#${buildNo})被拦截，需要审核\n所属项目：${projectName}\n拦截时间：${time} ${result}\n审核链接：${url}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("acd5c108730b41babcd57dbf31fda760","1dd8f74e334e4c41a20f4187b4e0e992","system",'',"DevOps","研发商店扩展【${name}】V${version} 成功发布","\n研发商店扩展【${name}】V${version} 成功发布","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("ad06d0496fb74f3f8c526e5ffda12d00","a9b46a21ed7949ddb946e5186b9102c1","system",'',"DevOps","研发商店扩展【${name}】V${version} 发布失败","研发商店扩展【${name}】V${version} 发布失败","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("b4d0f2eb62394a0d9df97b97858adf9f","4589771c46b248079444a0e18e46a3fe","system",'',"DevOps","研发商店镜像【${name}】V${version} 审核通过，成功发布","研发商店镜像【${name}】V${version} 审核通过，成功发布","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("bd286b16e530440fb5691ab8dc625274","d7d9e1a773fe4a658f6c51b80da282ea","system",'',"DevOps","【蓝盾质量红线】拦截通知","${pipelineName}(#${buildNo})被拦截\n所属项目：${projectName}\n拦截时间：${time}\n拦截指标：${thresholdListString}\n详情链接：${url}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("c7a45d36528a4af784f754017d7e37fe","7a64b8cd997b49858964ac34079bf234","system",'',"DevOps","【蓝盾质量红线】审核通知","${pipelineName}(#${buildNo})被拦截，需要审核\n所属项目：${projectName}\n拦截时间：${time} ${result}\n审核链接：${url}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("d4c60ef6f52c4afe8429a72c4b4ab425","13ec2ac58aab4d9bb8a04bbf7a9242f6","system",'',"DevOps","研发商店插件成员添加通知","研发商店的【${storeName}】插件的管理员【${storeAdmin}】已将您添加为该插件的成员","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("d6c50c2989944c9f8a16aa029ed816b0","168b9b44b7104647b2102355180a2837","system",'',"DevOps","【${projectName}】项目流水线实例更新","您在【${projectName}】项目下已成功更新${successPipelineNum}条实例：\n${successPipelineMsg}\n其中${failurePipelineNum}条实例更新失败：\n${failurePipelineMsg}\n\n查看详情：${instanceListUrl}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("e91eeea3f97345e69a19fafec806e75a","2e9227b122024a5a83c46a9f41b94aad","system",'',"DevOps","【蓝盾研发商店】评论通知","${userId} 在 ${storeName} 回复了 ${replyToUser}\n${replyContent}//@${replyToUser}: ${replyComment}\n[去回复 | ${url}]","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("f19f696753f14007b3733e36dcf5d476","f0663e7ac2b74130ada0408bdce8cd20","system",'',"DevOps","【蓝盾质量红线】拦截通知-已终止","${title}\n所属项目：${projectName}\n触发人：${cc}\n拦截时间：${time}\n${result}\n流水线链接：${url}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("f5861d3d23604c328aedd74afe73496c","96c0a1adaca7428eb0b2d69820923d52","system",'',"DevOps","研发商店扩展【${name}】V${version} 审核不通过","研发商店扩展【${name}】V${version} 审核不通过，驳回原因：${serviceStatusMsg}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("1424de9fcb6e4efe860e0b64bbf78002","d58e5895dfab4136ac898d93e3bff3ac","system",'',"DevOps","项目【${projectName}】下的流水线【${pipelineName}】#${buildNum} 构建触发审核","触发人：${triggerUser}\n\n审核人：${reviewers}\n\n电脑端点击 ${reviewUrl}\n手机端点击 ${reviewAppUrl}","2022-09-19 13:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("9fb98246b0a2478384c21ba3fb617474","7dee6377c27344818836ef1f035a14d1","system",'',"DevOps","项目【${projectName}】下的流水线【${pipelineName}】#${buildNum} 构建Stage审核","当前构建需人工审核通过后才执行后续流程，请耐心等待审核，或联系审核人${reviewers}处理。\n${reviewDesc} \n电脑端点击 ${reviewUrl} \n手机端点击 ${reviewAppUrl}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("db7ae51ad64f48fa878d36308cdecb8a","15a1e5f8642c4d7196270329531bf4cb","system",'',"DevOps","项目【${projectName}】下的流水线【${pipelineName}】#${buildNum} 构建Stage审核","构建已被「${rejectUserId}」驳回，流水线状态为「 Stage成功」。\n驳回理由：\n${suggest}\n电脑端点击 ${reviewUrl} \n手机端点击 ${reviewAppUrl}","2021-09-13 17:13:58",null);
REPLACE INTO  `T_WEWORK_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `SENDER`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("ca32a24efaf74062a0cd68a24f47e3b4","1e7864726dd04311b21019bcceddfee8","system",'',"DevOps","蓝盾流水线回调接口异常","项目【${projectName}】所注册的【${events}】事件回调接口【${callbackUrl}】因多次调用失败，现已被禁用\n流水线链接：${pipelineListUrl}","2023-04-26 17:00:00",null);

REPLACE INTO  `T_WEWORK_GROUP_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("b236e73c17cb48bbbd7cc38ffbcae407","15a1e5f8642c4d7196270329531bf4cb","system","DevOps","项目【${projectName}】下的流水线【${pipelineName}】#${buildNum} 构建Stage审核","构建已被「${rejectUserId}」驳回，流水线状态为「 Stage成功」。\n驳回理由： \n${suggest} \n[电脑端点击](${reviewUrl}) \n[手机端点击](${reviewAppUrl})","2023-01-29 17:13:58","2023-01-29 17:13:58");
REPLACE INTO  `T_WEWORK_GROUP_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("0ca84f7d0dcf4453b30b3e465f1c4caf","37ef4157cc254021ac7278b70f211f5e","system","DevOps","${content}","审核说明：\n${reviewDesc} \n电脑端：[去审核](${reviewUrl}) \n手机端：[去审核](${reviewAppUrl})","2023-01-29 17:13:58","2023-01-29 17:13:58");
REPLACE INTO  `T_WEWORK_GROUP_NOTIFY_MESSAGE_TEMPLATE` (`ID`, `COMMON_TEMPLATE_ID`, `CREATOR`, `MODIFIOR`, `TITLE`, `BODY`, `CREATE_TIME`, `UPDATE_TIME`) VALUES  ("cead07553b364b5a9c3f0290e35c82da","f0949ab70cc04aa5b1bc787d5a47eba7","system","DevOps","项目【${projectName}】下的流水线【${pipelineName}】#${buildNum} 构建Stage审核","构建状态为stage sucess，需要「${reviewers}」的审核才执行后续流程\n\n审核说明：\n${reviewDesc}\n\n[电脑端点击](${reviewUrl})\n[手机端点击](${reviewAppUrl})","2023-01-29 17:13:58","2023-01-29 17:13:58");
