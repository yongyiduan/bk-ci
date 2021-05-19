<template>
    <bk-exception :type="type" class="exception-home">
        <section class="exception-content">
            <span>{{ title }}</span>
            <span class="exception-title">
                {{ message }}
                <bk-link theme="primary" href="https://iwiki.woa.com/x/r3IyKQ" v-if="+$route.params.type === 499">Learn more</bk-link>
            </span>
            <bk-button theme="primary" v-if="+$route.params.type === 418" @click="oauth" :loading="isSaving">OAUTH 授权</bk-button>
        </section>
    </bk-exception>
</template>

<script>
    import { common } from '@/http'

    export default {
        data () {
            return {
                type: '404',
                message: '',
                title: '',
                isSaving: false
            }
        },

        computed: {
            projectPath () {
                return this.$route.params.workspace + '/' + this.$route.params.projectPath
            }
        },

        mounted () {
            this.initStatus()
        },

        methods: {
            initStatus () {
                const type = +this.$route.params.type || 404
                const typeMap = {
                    404: 404,
                    499: 'login',
                    500: 500
                }
                const messageMap = {
                    418: '尚未进行工蜂 OAUTH 授权，请先授权，在进行操作！',
                    403: `没有工蜂项目 ${this.projectPath} 的访问权限，请先加入项目!`,
                    499: 'Build, test, and deploy your code. continuous delivery of your product faster, easier, with fewer bugs. ',
                    500: '系统异常，请稍后重试'
                }
                const titleMap = {
                    403: '无业务权限',
                    404: '页面不存在',
                    418: '无权限',
                    499: 'Welcome to get started with Tencent CI.',
                    500: '系统异常'
                }
                this.type = typeMap[type] || '403'
                this.message = messageMap[type]
                this.title = titleMap[type]
            },

            oauth () {
                const redirectUrl = location.protocol + '//' + location.host + '/' + this.projectPath
                this.isSaving = true
                common.oauth(redirectUrl).then((res) => {
                    if (res.url) {
                        location.href = res.url
                    }
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
        margin-top: 50px;
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
