<template>
    <article class="build-detail-home">
        <section class="detail-header">
            <i class="bk-icon icon-check-1"></i>
            <p class="detail-info">
                <span class="info-title">feat: dockerhost-buildImage支持多个tag，支持--target</span>
                <span class="info-data">
                    <span class="info-item">master</span>
                    <span class="info-item">38 seconds</span>
                    <span class="info-item">3534deffe</span>
                    <span class="info-item">2021-03-23 23:12:38</span>
                </span>
            </p>
            <bk-button class="detail-button">重新构建</bk-button>
        </section>
        <stages :stages="stageList" class="detail-stages"></stages>
    </article>
</template>

<script>
    import stages from '@/components/stages'

    export default {
        components: {
            stages
        },

        data () {
            return {
                stageList: [
                    {
                        'containers': [
                            {
                                '@type': 'trigger',
                                'id': '0',
                                'name': '构建触发',
                                'elements': [
                                    {
                                        '@type': 'manualTrigger',
                                        'name': '手动触发',
                                        'id': 'T-1-1-1',
                                        'status': 'SUCCEED',
                                        'canElementSkip': false,
                                        'useLatestParameters': false,
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'version': '1.*',
                                        'classType': 'manualTrigger',
                                        'atomCode': 'manualTrigger',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    }
                                ],
                                'status': 'SUCCEED',
                                'systemElapsed': 308,
                                'elementElapsed': 0,
                                'params': [
                                    {
                                        'id': 'param61',
                                        'required': true,
                                        'type': 'GIT_REF',
                                        'defaultValue': '',
                                        'options': [],
                                        'desc': '',
                                        'repoHashId': '19yN'
                                    },
                                    {
                                        'id': 'param60',
                                        'required': true,
                                        'type': 'STRING',
                                        'defaultValue': 'value',
                                        'desc': ''
                                    },
                                    {
                                        'id': 'BK_CI_MAJOR_VERSION',
                                        'required': false,
                                        'type': 'STRING',
                                        'defaultValue': '0',
                                        'desc': '主版本'
                                    },
                                    {
                                        'id': 'BK_CI_MINOR_VERSION',
                                        'required': false,
                                        'type': 'STRING',
                                        'defaultValue': '0',
                                        'desc': '特性版本'
                                    },
                                    {
                                        'id': 'BK_CI_FIX_VERSION',
                                        'required': false,
                                        'type': 'STRING',
                                        'defaultValue': '0',
                                        'desc': '修正版本'
                                    }
                                ],
                                'templateParams': [],
                                'buildNo': {
                                    'buildNo': 5,
                                    'buildNoType': 'EVERY_BUILD_INCREMENT',
                                    'required': true
                                },
                                'canRetry': false,
                                'containerId': 'c-1b9ed0e0da49432bbe1a3536c907c4c8',
                                'startVMStatus': 'SUCCEED',
                                'executeCount': 1,
                                'classType': 'trigger'
                            }
                        ],
                        'id': 'stage-1',
                        'name': 'stage-1',
                        'tag': [
                            'e111cb2340134e0b9b9f8dd589d69435'
                        ],
                        'status': 'SUCCEED',
                        'elapsed': 308,
                        'fastKill': false,
                        'finally': false,
                        'canRetry': false
                    },
                    {
                        'containers': [
                            {
                                '@type': 'vmBuild',
                                'id': '1',
                                'name': '构建环境-LINUX',
                                'elements': [
                                    {
                                        '@type': 'linuxScript',
                                        'name': 'Bash',
                                        'id': 'e-0350056b837e427abc209c153b0181ad',
                                        'status': 'SUCCEED',
                                        'scriptType': 'SHELL',
                                        'script': '# 通过./xxx.sh的方式执行脚本\n# 即若脚本中未指定解释器，则使用系统默认的shell\n\n# 您可以通过setEnv函数设置插件间传递的参数\n# setEnv \'FILENAME\' \'package.zip\'\n# 然后在后续的插件的表单中使用${FILENAME}引用这个变量\n\n# 您可以在质量红线中创建自定义指标，然后通过setGateValue函数设置指标值\n# setGateValue \'CodeCoverage\' $myValue\n# 然后在质量红线选择相应指标和阈值。若不满足，流水线在执行时将会被卡住\n\n# cd ${WORKSPACE} 可进入当前工作空间目录\necho \'Hello Hello Hello!\'>>1.html\n',
                                        'continueNoneZero': false,
                                        'enableArchiveFile': false,
                                        'archiveFile': '',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 729,
                                        'startEpoch': 1620822438300,
                                        'version': '1.*',
                                        'classType': 'linuxScript',
                                        'atomCode': 'linuxScript',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'reportArchive',
                                        'name': '归档报告',
                                        'id': 'e-b924655da4be4130914450c4fa2dd50b',
                                        'status': 'SUCCEED',
                                        'fileDir': './',
                                        'indexFile': '1.html',
                                        'reportName': 'aa',
                                        'enableEmail': false,
                                        'emailReceivers': [],
                                        'emailTitle': '【${pipeline.name}】  #${pipeline.build.num}自定义报告已归档',
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 970,
                                        'startEpoch': 1620822439251,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'reportArchive',
                                        'atomCode': 'reportArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'singleArchive',
                                        'name': '归档构件',
                                        'id': 'e-bda8a693bb234360a28830e5eef6aea7',
                                        'status': 'SUCCEED',
                                        'filePath': '*.html',
                                        'destPath': './',
                                        'customize': false,
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 260,
                                        'startEpoch': 1620822440452,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'singleArchive',
                                        'atomCode': 'singleArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    }
                                ],
                                'status': 'SUCCEED',
                                'startEpoch': 1620822412826,
                                'systemElapsed': 24027,
                                'elementElapsed': 1959,
                                'baseOS': 'LINUX',
                                'vmNames': [],
                                'maxQueueMinutes': 60,
                                'maxRunningMinutes': 900,
                                'buildEnv': {},
                                'dispatchType': {
                                    'buildType': 'PUBLIC_DEVCLOUD',
                                    'value': 'devcloudtlinux2_2',
                                    'imageType': 'BKSTORE',
                                    'credentialId': '',
                                    'credentialProject': '',
                                    'imageCode': 'devcloudtlinux2_2',
                                    'imageVersion': '1.*',
                                    'imageName': 'devcloud-tlinux2.2',
                                    'dockerBuildVersion': 'devcloudtlinux2_2',
                                    'imagePublicFlag': false,
                                    'imageRDType': '',
                                    'recommendFlag': true
                                },
                                'showBuildResource': false,
                                'canRetry': false,
                                'enableExternal': false,
                                'containerId': 'c-fba2e2c7d35d443e861d2e8195754396',
                                'jobControlOption': {
                                    'enable': true,
                                    'prepareTimeout': 10,
                                    'timeout': 900,
                                    'runCondition': 'STAGE_RUNNING',
                                    'customVariables': [],
                                    'continueWhenFailed': false
                                },
                                'startVMStatus': 'SUCCEED',
                                'executeCount': 0,
                                'nfsSwitch': true,
                                'classType': 'vmBuild'
                            },
                            {
                                '@type': 'vmBuild',
                                'id': '2',
                                'name': '构建环境-LINUX',
                                'elements': [
                                    {
                                        '@type': 'linuxScript',
                                        'name': 'Bash',
                                        'id': 'e-2e04c1e307f84f37a014d090d3750964',
                                        'status': 'SUCCEED',
                                        'scriptType': 'SHELL',
                                        'script': '# 通过./xxx.sh的方式执行脚本\n# 即若脚本中未指定解释器，则使用系统默认的shell\n\n# 您可以通过setEnv函数设置插件间传递的参数\n# setEnv \'FILENAME\' \'package.zip\'\n# 然后在后续的插件的表单中使用${FILENAME}引用这个变量\n\n# 您可以在质量红线中创建自定义指标，然后通过setGateValue函数设置指标值\n# setGateValue \'CodeCoverage\' $myValue\n# 然后在质量红线选择相应指标和阈值。若不满足，流水线在执行时将会被卡住\n\n# cd ${WORKSPACE} 可进入当前工作空间目录\necho \'Hello Hello Hello!\'>>1.html\n',
                                        'continueNoneZero': false,
                                        'enableArchiveFile': false,
                                        'archiveFile': '',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 654,
                                        'startEpoch': 1620822427220,
                                        'version': '1.*',
                                        'classType': 'linuxScript',
                                        'atomCode': 'linuxScript',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'reportArchive',
                                        'name': '归档报告',
                                        'id': 'e-3419912714e743e5acba4619fb733bfc',
                                        'status': 'SUCCEED',
                                        'fileDir': './',
                                        'indexFile': '1.html',
                                        'reportName': 'aa',
                                        'enableEmail': false,
                                        'emailReceivers': [],
                                        'emailTitle': '【${pipeline.name}】  #${pipeline.build.num}自定义报告已归档',
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 1185,
                                        'startEpoch': 1620822428099,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'reportArchive',
                                        'atomCode': 'reportArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'singleArchive',
                                        'name': '归档构件',
                                        'id': 'e-7382d07df86e44ffb6517b4f42164d1d',
                                        'status': 'SUCCEED',
                                        'filePath': '*.html',
                                        'destPath': './',
                                        'customize': false,
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 449,
                                        'startEpoch': 1620822429541,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'singleArchive',
                                        'atomCode': 'singleArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    }
                                ],
                                'status': 'SUCCEED',
                                'startEpoch': 1620822412616,
                                'elementElapsed': 2288,
                                'baseOS': 'LINUX',
                                'vmNames': [],
                                'maxQueueMinutes': 60,
                                'maxRunningMinutes': 900,
                                'buildEnv': {},
                                'dispatchType': {
                                    'buildType': 'PUBLIC_DEVCLOUD',
                                    'value': 'devcloudtlinux2_2',
                                    'imageType': 'BKSTORE',
                                    'credentialId': '',
                                    'credentialProject': '',
                                    'imageCode': 'devcloudtlinux2_2',
                                    'imageVersion': '1.*',
                                    'imageName': 'devcloud-tlinux2.2',
                                    'dockerBuildVersion': 'devcloudtlinux2_2',
                                    'imagePublicFlag': false,
                                    'imageRDType': '',
                                    'recommendFlag': true
                                },
                                'showBuildResource': false,
                                'canRetry': false,
                                'enableExternal': false,
                                'containerId': 'c-27a9e7e9669d4f0d8f3d06602730713d',
                                'jobControlOption': {
                                    'enable': true,
                                    'prepareTimeout': 10,
                                    'timeout': 900,
                                    'runCondition': 'STAGE_RUNNING',
                                    'customVariables': [],
                                    'continueWhenFailed': false
                                },
                                'startVMStatus': 'SUCCEED',
                                'executeCount': 0,
                                'jobId': 'job_jXx',
                                'nfsSwitch': true,
                                'classType': 'vmBuild'
                            }
                        ],
                        'id': 'stage-2',
                        'name': 'stage-2',
                        'tag': [
                            'e111cb2340134e0b9b9f8dd589d69435'
                        ],
                        'status': 'SUCCEED',
                        'startEpoch': 1620822412473,
                        'elapsed': 31541,
                        'fastKill': false,
                        'finally': false,
                        'canRetry': false
                    },
                    {
                        'containers': [
                            {
                                '@type': 'vmBuild',
                                'id': '3',
                                'name': '构建环境-LINUX',
                                'elements': [
                                    {
                                        '@type': 'linuxScript',
                                        'name': 'Bash',
                                        'id': 'e-512533319a8b4f369274e8f1413e7f21',
                                        'status': 'SUCCEED',
                                        'scriptType': 'SHELL',
                                        'script': '# 通过./xxx.sh的方式执行脚本\n# 即若脚本中未指定解释器，则使用系统默认的shell\n\n# 您可以通过setEnv函数设置插件间传递的参数\n# setEnv \'FILENAME\' \'package.zip\'\n# 然后在后续的插件的表单中使用${FILENAME}引用这个变量\n\n# 您可以在质量红线中创建自定义指标，然后通过setGateValue函数设置指标值\n# setGateValue \'CodeCoverage\' $myValue\n# 然后在质量红线选择相应指标和阈值。若不满足，流水线在执行时将会被卡住\n\n# cd ${WORKSPACE} 可进入当前工作空间目录\necho \'Hello Hello Hello!\'>>1.html\n',
                                        'continueNoneZero': false,
                                        'enableArchiveFile': false,
                                        'archiveFile': '',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 519,
                                        'startEpoch': 1620822456760,
                                        'version': '1.*',
                                        'classType': 'linuxScript',
                                        'atomCode': 'linuxScript',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'reportArchive',
                                        'name': '归档报告',
                                        'id': 'e-007970efae9c467aabf582d5e18bd709',
                                        'status': 'SUCCEED',
                                        'fileDir': './',
                                        'indexFile': '1.html',
                                        'reportName': 'aa',
                                        'enableEmail': false,
                                        'emailReceivers': [],
                                        'emailTitle': '【${pipeline.name}】  #${pipeline.build.num}自定义报告已归档',
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 997,
                                        'startEpoch': 1620822457503,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'reportArchive',
                                        'atomCode': 'reportArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'singleArchive',
                                        'name': '归档构件',
                                        'id': 'e-52388c5c33b048a2a5d0303ed0120cbe',
                                        'status': 'SUCCEED',
                                        'filePath': '*.html',
                                        'destPath': './',
                                        'customize': false,
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 347,
                                        'startEpoch': 1620822458758,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'singleArchive',
                                        'atomCode': 'singleArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    }
                                ],
                                'status': 'SUCCEED',
                                'startEpoch': 1620822444272,
                                'elementElapsed': 1863,
                                'baseOS': 'LINUX',
                                'vmNames': [],
                                'maxQueueMinutes': 60,
                                'maxRunningMinutes': 900,
                                'buildEnv': {},
                                'dispatchType': {
                                    'buildType': 'PUBLIC_DEVCLOUD',
                                    'value': 'devcloudtlinux2_2',
                                    'imageType': 'BKSTORE',
                                    'credentialId': '',
                                    'credentialProject': '',
                                    'imageCode': 'devcloudtlinux2_2',
                                    'imageVersion': '1.*',
                                    'imageName': 'devcloud-tlinux2.2',
                                    'dockerBuildVersion': 'devcloudtlinux2_2',
                                    'imagePublicFlag': false,
                                    'imageRDType': '',
                                    'recommendFlag': true
                                },
                                'showBuildResource': false,
                                'canRetry': false,
                                'enableExternal': false,
                                'containerId': 'c-bee7e9baf80c4acbbfd8d00e20457796',
                                'jobControlOption': {
                                    'enable': true,
                                    'prepareTimeout': 10,
                                    'timeout': 900,
                                    'runCondition': 'STAGE_RUNNING',
                                    'customVariables': [],
                                    'continueWhenFailed': false
                                },
                                'startVMStatus': 'SUCCEED',
                                'executeCount': 0,
                                'jobId': 'job_xlX',
                                'nfsSwitch': true,
                                'classType': 'vmBuild'
                            }
                        ],
                        'id': 'stage-3',
                        'name': 'stage-2',
                        'tag': [
                            'e111cb2340134e0b9b9f8dd589d69435'
                        ],
                        'status': 'SUCCEED',
                        'startEpoch': 1620822444109,
                        'elapsed': 18236,
                        'fastKill': false,
                        'finally': false,
                        'canRetry': false
                    },
                    {
                        'containers': [
                            {
                                '@type': 'vmBuild',
                                'id': '3',
                                'name': '构建环境-LINUX',
                                'elements': [
                                    {
                                        '@type': 'linuxScript',
                                        'name': 'Bash',
                                        'id': 'e-512533319a8b4f369274e8f1413e7f21',
                                        'status': 'SUCCEED',
                                        'scriptType': 'SHELL',
                                        'script': '# 通过./xxx.sh的方式执行脚本\n# 即若脚本中未指定解释器，则使用系统默认的shell\n\n# 您可以通过setEnv函数设置插件间传递的参数\n# setEnv \'FILENAME\' \'package.zip\'\n# 然后在后续的插件的表单中使用${FILENAME}引用这个变量\n\n# 您可以在质量红线中创建自定义指标，然后通过setGateValue函数设置指标值\n# setGateValue \'CodeCoverage\' $myValue\n# 然后在质量红线选择相应指标和阈值。若不满足，流水线在执行时将会被卡住\n\n# cd ${WORKSPACE} 可进入当前工作空间目录\necho \'Hello Hello Hello!\'>>1.html\n',
                                        'continueNoneZero': false,
                                        'enableArchiveFile': false,
                                        'archiveFile': '',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 519,
                                        'startEpoch': 1620822456760,
                                        'version': '1.*',
                                        'classType': 'linuxScript',
                                        'atomCode': 'linuxScript',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'reportArchive',
                                        'name': '归档报告',
                                        'id': 'e-007970efae9c467aabf582d5e18bd709',
                                        'status': 'SUCCEED',
                                        'fileDir': './',
                                        'indexFile': '1.html',
                                        'reportName': 'aa',
                                        'enableEmail': false,
                                        'emailReceivers': [],
                                        'emailTitle': '【${pipeline.name}】  #${pipeline.build.num}自定义报告已归档',
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 997,
                                        'startEpoch': 1620822457503,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'reportArchive',
                                        'atomCode': 'reportArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'singleArchive',
                                        'name': '归档构件',
                                        'id': 'e-52388c5c33b048a2a5d0303ed0120cbe',
                                        'status': 'SUCCEED',
                                        'filePath': '*.html',
                                        'destPath': './',
                                        'customize': false,
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 347,
                                        'startEpoch': 1620822458758,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'singleArchive',
                                        'atomCode': 'singleArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    }
                                ],
                                'status': 'SUCCEED',
                                'startEpoch': 1620822444272,
                                'elementElapsed': 1863,
                                'baseOS': 'LINUX',
                                'vmNames': [],
                                'maxQueueMinutes': 60,
                                'maxRunningMinutes': 900,
                                'buildEnv': {},
                                'dispatchType': {
                                    'buildType': 'PUBLIC_DEVCLOUD',
                                    'value': 'devcloudtlinux2_2',
                                    'imageType': 'BKSTORE',
                                    'credentialId': '',
                                    'credentialProject': '',
                                    'imageCode': 'devcloudtlinux2_2',
                                    'imageVersion': '1.*',
                                    'imageName': 'devcloud-tlinux2.2',
                                    'dockerBuildVersion': 'devcloudtlinux2_2',
                                    'imagePublicFlag': false,
                                    'imageRDType': '',
                                    'recommendFlag': true
                                },
                                'showBuildResource': false,
                                'canRetry': false,
                                'enableExternal': false,
                                'containerId': 'c-bee7e9baf80c4acbbfd8d00e20457796',
                                'jobControlOption': {
                                    'enable': true,
                                    'prepareTimeout': 10,
                                    'timeout': 900,
                                    'runCondition': 'STAGE_RUNNING',
                                    'customVariables': [],
                                    'continueWhenFailed': false
                                },
                                'startVMStatus': 'SUCCEED',
                                'executeCount': 0,
                                'jobId': 'job_xlX',
                                'nfsSwitch': true,
                                'classType': 'vmBuild'
                            }
                        ],
                        'id': 'stage-3',
                        'name': 'stage-2',
                        'tag': [
                            'e111cb2340134e0b9b9f8dd589d69435'
                        ],
                        'status': 'SUCCEED',
                        'startEpoch': 1620822444109,
                        'elapsed': 18236,
                        'fastKill': false,
                        'finally': false,
                        'canRetry': false
                    },
                    {
                        'containers': [
                            {
                                '@type': 'vmBuild',
                                'id': '3',
                                'name': '构建环境-LINUX',
                                'elements': [
                                    {
                                        '@type': 'linuxScript',
                                        'name': 'Bash',
                                        'id': 'e-512533319a8b4f369274e8f1413e7f21',
                                        'status': 'SUCCEED',
                                        'scriptType': 'SHELL',
                                        'script': '# 通过./xxx.sh的方式执行脚本\n# 即若脚本中未指定解释器，则使用系统默认的shell\n\n# 您可以通过setEnv函数设置插件间传递的参数\n# setEnv \'FILENAME\' \'package.zip\'\n# 然后在后续的插件的表单中使用${FILENAME}引用这个变量\n\n# 您可以在质量红线中创建自定义指标，然后通过setGateValue函数设置指标值\n# setGateValue \'CodeCoverage\' $myValue\n# 然后在质量红线选择相应指标和阈值。若不满足，流水线在执行时将会被卡住\n\n# cd ${WORKSPACE} 可进入当前工作空间目录\necho \'Hello Hello Hello!\'>>1.html\n',
                                        'continueNoneZero': false,
                                        'enableArchiveFile': false,
                                        'archiveFile': '',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 519,
                                        'startEpoch': 1620822456760,
                                        'version': '1.*',
                                        'classType': 'linuxScript',
                                        'atomCode': 'linuxScript',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'reportArchive',
                                        'name': '归档报告',
                                        'id': 'e-007970efae9c467aabf582d5e18bd709',
                                        'status': 'SUCCEED',
                                        'fileDir': './',
                                        'indexFile': '1.html',
                                        'reportName': 'aa',
                                        'enableEmail': false,
                                        'emailReceivers': [],
                                        'emailTitle': '【${pipeline.name}】  #${pipeline.build.num}自定义报告已归档',
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 997,
                                        'startEpoch': 1620822457503,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'reportArchive',
                                        'atomCode': 'reportArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    },
                                    {
                                        '@type': 'singleArchive',
                                        'name': '归档构件',
                                        'id': 'e-52388c5c33b048a2a5d0303ed0120cbe',
                                        'status': 'SUCCEED',
                                        'filePath': '*.html',
                                        'destPath': './',
                                        'customize': false,
                                        'executeCount': 1,
                                        'canRetry': false,
                                        'elapsed': 347,
                                        'startEpoch': 1620822458758,
                                        'version': '1.*',
                                        'additionalOptions': {
                                            'enable': true,
                                            'continueWhenFailed': false,
                                            'retryWhenFailed': false,
                                            'retryCount': 1,
                                            'timeout': 900,
                                            'runCondition': 'PRE_TASK_SUCCESS',
                                            'pauseBeforeExec': false,
                                            'subscriptionPauseUser': '',
                                            'otherTask': '',
                                            'customVariables': [
                                                {
                                                    'key': 'param1',
                                                    'value': ''
                                                }
                                            ],
                                            'customCondition': '',
                                            'enableCustomEnv': false
                                        },
                                        'classType': 'singleArchive',
                                        'atomCode': 'singleArchive',
                                        'elementEnable': true,
                                        'taskAtom': ''
                                    }
                                ],
                                'status': 'SUCCEED',
                                'startEpoch': 1620822444272,
                                'elementElapsed': 1863,
                                'baseOS': 'LINUX',
                                'vmNames': [],
                                'maxQueueMinutes': 60,
                                'maxRunningMinutes': 900,
                                'buildEnv': {},
                                'dispatchType': {
                                    'buildType': 'PUBLIC_DEVCLOUD',
                                    'value': 'devcloudtlinux2_2',
                                    'imageType': 'BKSTORE',
                                    'credentialId': '',
                                    'credentialProject': '',
                                    'imageCode': 'devcloudtlinux2_2',
                                    'imageVersion': '1.*',
                                    'imageName': 'devcloud-tlinux2.2',
                                    'dockerBuildVersion': 'devcloudtlinux2_2',
                                    'imagePublicFlag': false,
                                    'imageRDType': '',
                                    'recommendFlag': true
                                },
                                'showBuildResource': false,
                                'canRetry': false,
                                'enableExternal': false,
                                'containerId': 'c-bee7e9baf80c4acbbfd8d00e20457796',
                                'jobControlOption': {
                                    'enable': true,
                                    'prepareTimeout': 10,
                                    'timeout': 900,
                                    'runCondition': 'STAGE_RUNNING',
                                    'customVariables': [],
                                    'continueWhenFailed': false
                                },
                                'startVMStatus': 'SUCCEED',
                                'executeCount': 0,
                                'jobId': 'job_xlX',
                                'nfsSwitch': true,
                                'classType': 'vmBuild'
                            }
                        ],
                        'id': 'stage-3',
                        'name': 'stage-2',
                        'tag': [
                            'e111cb2340134e0b9b9f8dd589d69435'
                        ],
                        'status': 'SUCCEED',
                        'startEpoch': 1620822444109,
                        'elapsed': 18236,
                        'fastKill': false,
                        'finally': false,
                        'canRetry': false
                    }
                ]
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .build-detail-home {
        background: #fff;
    }
    .detail-header {
        padding: 16px 24px;
        height: 96px;
        box-shadow: 0 1px 0 0 #E3E5EA;
        display: flex;
        align-items: flex-start;
        .icon-check-1 {
            font-size: 32px;
        }
        .detail-info {
            flex: 1;
            margin: 0 7px;
            .info-title {
                color: #313328;
                line-height: 24px;
                height: 24px;
                display: inline-block;
                margin: 4px 0 10px;
            }
            .info-data {
                color: #81838a;
                line-height: 20px;
                font-size: 12px;
                display: flex;
                align-items: center;
            }
            .info-item:not(:first-child) {
                margin-left: 40px;
            }
        }
        .detail-button {
            margin-top: 16px;
        }
    }
    .detail-stages {
        height: calc(100% - 96px);
        padding: 30px 0;
        margin: 0 30px;
        overflow: auto;
    }
</style>
