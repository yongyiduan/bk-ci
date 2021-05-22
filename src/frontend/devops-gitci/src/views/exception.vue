<template>
    <bk-exception :type="type" class="exception-home">
        <section class="exception-content">
            <span>{{ title }}</span>
            <span class="exception-title">
                {{ message }}
                <bk-link theme="primary" href="https://iwiki.woa.com/x/r3IyKQ" v-if="exceptionInfo.type === 499">Learn more</bk-link>
            </span>
            <bk-button theme="primary" v-if="exceptionInfo.type === 418" @click="oauth" :loading="isSaving">OAUTH 授权</bk-button>
            <bk-button theme="primary" v-if="exceptionInfo.type === 419" @click="enable" :loading="isSaving">开启</bk-button>
        </section>
    </bk-exception>
</template>

<script>
    import { common, setting } from '@/http'
    import { mapState } from 'vuex'

    export default {
        data () {
            return {
                isSaving: false,
                typeMap: {
                    404: 404,
                    499: 'login',
                    500: 500
                },
                titleMap: {
                    403: '无业务权限',
                    404: '页面不存在',
                    418: '无权限',
                    419: '未开启 Git Ci',
                    499: 'Welcome to Tencent CI.',
                    500: '系统异常'
                }
            }
        },

        computed: {
            ...mapState(['exceptionInfo', 'projectId']),

            projectPath () {
                return (this.$route.hash || '').slice(1)
            },

            type () {
                const type = this.exceptionInfo.type || 404
                return this.typeMap[type] || '403'
            },

            message () {
                const type = this.exceptionInfo.type || 404
                return this.messageMap[type]
            },

            title () {
                const type = this.exceptionInfo.type || 404
                return this.titleMap[type]
            },

            messageMap () {
                return {
                    418: '尚未进行工蜂 OAUTH 授权，请先授权，在进行操作！',
                    403: `没有工蜂项目 ${this.projectPath} 的访问权限，请先加入项目！`,
                    499: 'Build, test, and deploy your code. continuous delivery of your product faster, easier, with fewer bugs. ',
                    500: '系统异常，请稍后重试',
                    419: '尚未开启 Git Ci，请先开启，在进行操作！'
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
                setting.saveSetting({ projectId: this.projectId, enableCi: true }).then(() => {
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
