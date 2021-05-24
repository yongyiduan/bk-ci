<template>
    <bk-exception :type="infoMap.typeMap[exceptionType] || '403'" class="exception-home">
        <section class="exception-content">
            <span>{{ infoMap.titleMap[exceptionType] }}</span>
            <span class="exception-title">
                {{ infoMap.messageMap[exceptionType] || exceptionInfo.message || '系统异常，请稍后重试！' }}
                <bk-link theme="primary" target="blank" href="https://iwiki.woa.com/x/r3IyKQ" v-if="exceptionType === 520">Learn more</bk-link>
            </span>
            <bk-button theme="primary" v-if="exceptionType === 418" @click="oauth" :loading="isSaving">OAUTH 授权</bk-button>
            <bk-button theme="primary" v-if="exceptionType === 419" @click="enable" :loading="isSaving">开启</bk-button>
        </section>
    </bk-exception>
</template>

<script>
    import { common, setting } from '@/http'
    import { mapState } from 'vuex'

    export default {
        data () {
            return {
                isSaving: false
            }
        },

        computed: {
            ...mapState(['exceptionInfo', 'projectId', 'projectInfo']),

            projectPath () {
                return (this.$route.hash || '').slice(1)
            },

            exceptionType () {
                return +this.exceptionInfo.type || 404
            },

            infoMap () {
                return {
                    typeMap: {
                        404: 404,
                        500: 500,
                        520: 'login'
                    },
                    titleMap: {
                        403: '无业务权限',
                        404: '页面不存在',
                        418: '无权限',
                        419: '未开启 CI',
                        499: '未查询到项目信息',
                        500: '系统异常',
                        520: 'Welcome to Tencent CI.'
                    },
                    messageMap: {
                        403: `没有工蜂项目 ${this.projectPath} 的访问权限，请先加入项目！`,
                        418: '尚未进行工蜂 OAUTH 授权，请先授权，再进行操作！',
                        419: '尚未开启 CI，请先开启，再进行操作！',
                        499: `未查询到工蜂项目 ${this.projectPath} 的信息，请修改后重试`,
                        520: 'Build, test, and deploy your code. continuous delivery of your product faster, easier, with fewer bugs. '
                    }
                }
            }
        },

        methods: {
            oauth () {
                this.isSaving = true
                common.oauth(location.href).then((res) => {
                    if (res.url) {
                        location.href = res.url
                    }
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isSaving = false
                })
            },

            enable () {
                this.isSaving = true
                setting.toggleEnableCi(true, this.projectInfo).then(() => {
                    location.reload()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isSaving = false
                })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .exception-home {
        margin-top: calc(50vh - 300px);
    }
    .exception-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        .exception-title {
            font-size: 14px;
            margin: 15px 0;
        }
    }
</style>
